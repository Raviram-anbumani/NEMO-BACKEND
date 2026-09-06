package com.nemo.backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.SQLVerificationRequest;
import com.nemo.backend.dto.SQLVerificationResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.model.quiz.QuizRound;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.QuestionProgressRepository;

@Service
public class SQLVerificationService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionProgressRepository questionProgressRepository;
    private final QuizDataService quizDataService;
    private final QuizProgressionService quizProgressionService;
    private final SQLSandboxService sqlSandboxService;

    public SQLVerificationService(
            GameSessionRepository gameSessionRepository,
            QuestionProgressRepository questionProgressRepository,
            QuizDataService quizDataService,
            QuizProgressionService quizProgressionService,
            SQLSandboxService sqlSandboxService
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionProgressRepository = questionProgressRepository;
        this.quizDataService = quizDataService;
        this.quizProgressionService = quizProgressionService;
        this.sqlSandboxService = sqlSandboxService;
    }

    @Transactional
    public SQLVerificationResponse verify(
            SQLVerificationRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be empty"
            );
        }

        if (request.getSessionId() == null ||
                request.getSessionId().isBlank()) {

            throw new IllegalArgumentException(
                    "Session ID is required"
            );
        }

        if (request.getQuestionId() == null ||
                request.getQuestionId().isBlank()) {

            throw new IllegalArgumentException(
                    "Question ID is required"
            );
        }

        if (request.getSqlQuery() == null ||
                request.getSqlQuery().isBlank()) {

            throw new IllegalArgumentException(
                    "SQL query cannot be empty"
            );
        }

        GameSession session =
                gameSessionRepository.findById(
                        request.getSessionId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "Session not found"
                        )
                );

        if (session.getStatus() != GameSession.Status.ACTIVE) {

            throw new IllegalArgumentException(
                    "Session is not active"
            );
        }

        QuizQuestion question =
                quizDataService.getQuestion(
                        request.getQuestionId()
                );

        if (!"sql".equalsIgnoreCase(
                question.getType()
        )) {

            throw new IllegalArgumentException(
                    "Question is not an SQL question"
            );
        }

        if (!quizProgressionService.isQuestionUnlocked(
                session,
                question
        )) {

            throw new IllegalArgumentException(
                    "Question is currently locked"
            );
        }

        QuestionProgress progress =
                questionProgressRepository
                        .findBySessionAndQuestionId(
                                session,
                                question.getId()
                        )
                        .orElseGet(() -> {

                            QuestionProgress newProgress =
                                    new QuestionProgress();

                            newProgress.setSession(session);
                            newProgress.setQuestionId(
                                    question.getId()
                            );

                            return newProgress;
                        });

        if (Boolean.TRUE.equals(
                progress.getCorrect()
        )) {

            return buildResponse(
                    session,
                    progress,
                    true,
                    "Question already completed.",
                    question.getExplanation(),
                    List.of()
            );
        }

        progress.setAttemptCount(
                progress.getAttemptCount() + 1
        );

        List<Map<String, Object>> rows;

        try {

            rows = sqlSandboxService.executeQuery(
                    question.getId(),
                    request.getSqlQuery()
            );

        } catch (IllegalArgumentException exception) {

            questionProgressRepository.save(progress);

            return buildResponse(
                    session,
                    progress,
                    false,
                    "Invalid SQL query.",
                    null,
                    List.of()
            );
        }

        boolean correct =
                matchesExpectedResult(
                        question.getId(),
                        rows
                );

        progress.setCorrect(correct);

        questionProgressRepository.save(progress);

        if (correct) {

            return buildResponse(
                    session,
                    progress,
                    true,
                    "Correct answer!",
                    question.getExplanation(),
                    rows
            );
        }

        return buildResponse(
                session,
                progress,
                false,
                "Incorrect result. Try again.",
                null,
                rows
        );
    }

    private boolean matchesExpectedResult(
            String questionId,
            List<Map<String, Object>> rows
    ) {

        if ("Q7".equalsIgnoreCase(questionId)) {

            return matchesQ7(rows);
        }

        if ("Q15".equalsIgnoreCase(questionId)) {

            return matchesQ15(rows);
        }

        return false;
    }

    private boolean matchesQ7(
            List<Map<String, Object>> rows
    ) {

        List<List<String>> expected =
                List.of(
                        List.of("henry"),
                        List.of("max")
                );

        return rowsMatch(
                rows,
                expected
        );
    }

    private boolean matchesQ15(
            List<Map<String, Object>> rows
    ) {

        List<List<String>> expected =
                List.of(
                        List.of("1", "root"),
                        List.of("2", "inner"),
                        List.of("3", "leaf"),
                        List.of("4", "leaf"),
                        List.of("5", "leaf")
                );

        return rowsMatch(
                rows,
                expected
        );
    }

    private boolean rowsMatch(
            List<Map<String, Object>> actualRows,
            List<List<String>> expectedRows
    ) {

        if (actualRows.size() != expectedRows.size()) {
            return false;
        }

        List<List<String>> actual =
                new ArrayList<>();

        for (Map<String, Object> row : actualRows) {

            List<String> values =
                    new ArrayList<>();

            for (Object value : row.values()) {

                if (value == null) {
                    values.add("null");
                } else {
                    values.add(
                            value.toString()
                                    .trim()
                                    .toLowerCase()
                    );
                }
            }

            actual.add(values);
        }

        actual.sort(
                (a, b) ->
                        String.join("|", a)
                                .compareTo(
                                        String.join("|", b)
                                )
        );

        List<List<String>> expected =
                new ArrayList<>(
                        expectedRows
                );

        expected.sort(
                (a, b) ->
                        String.join("|", a)
                                .compareTo(
                                        String.join("|", b)
                                )
        );

        return actual.equals(expected);
    }

    private SQLVerificationResponse buildResponse(
            GameSession session,
            QuestionProgress progress,
            boolean correct,
            String message,
            String explanation,
            List<Map<String, Object>> rows
    ) {

        List<QuestionProgress> allProgress =
                questionProgressRepository
                        .findBySession(session);

        List<String> completedQuestionIds =
                allProgress.stream()
                        .filter(p ->
                                Boolean.TRUE.equals(
                                        p.getCorrect()
                                )
                        )
                        .map(QuestionProgress::getQuestionId)
                        .toList();

        List<Integer> completedRounds =
                quizDataService.getRounds()
                        .stream()
                        .filter(round ->
                                round.getQuestions()
                                        .stream()
                                        .allMatch(question ->
                                                completedQuestionIds
                                                        .contains(
                                                                question.getId()
                                                        )
                                        )
                        )
                        .map(QuizRound::getId)
                        .toList();

        return new SQLVerificationResponse(
                true,
                correct,
                progress.getQuestionId(),
                message,
                explanation,
                session.getTotalActiveTimeMs(),
                completedQuestionIds,
                completedRounds,
                rows
        );
    }
}