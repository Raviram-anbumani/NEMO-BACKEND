package com.nemo.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.RiddleVerificationRequest;
import com.nemo.backend.dto.VerificationResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.model.quiz.QuizRound;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.QuestionProgressRepository;

import tools.jackson.databind.JsonNode;

@Service
public class RiddleVerificationService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionProgressRepository questionProgressRepository;
    private final QuizDataService quizDataService;
    private final QuizProgressionService quizProgressionService;

    public RiddleVerificationService(
            GameSessionRepository gameSessionRepository,
            QuestionProgressRepository questionProgressRepository,
            QuizDataService quizDataService,
            QuizProgressionService quizProgressionService
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionProgressRepository = questionProgressRepository;
        this.quizDataService = quizDataService;
        this.quizProgressionService = quizProgressionService;
    }

    @Transactional
    public VerificationResponse verify(
            RiddleVerificationRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be empty"
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

        if (request.getQuestionId() == null ||
                request.getQuestionId().isBlank()) {

            throw new IllegalArgumentException(
                    "Question ID is required"
            );
        }

        QuizQuestion question =
                quizDataService.getQuestion(
                        request.getQuestionId()
                );

        if (!"text_riddle".equalsIgnoreCase(
                question.getType()
        )) {
            throw new IllegalArgumentException(
                    "Question is not a riddle"
            );
        }

        /*
         * Backend-enforced sequential progression.
         */
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

        /*
         * Once correctly solved, the question remains
         * permanently solved for this session.
         */
        if (Boolean.TRUE.equals(progress.getCorrect())) {

            return buildResponse(
                    session,
                    progress,
                    true,
                    "Question already completed.",
                    question.getExplanation()
            );
        }

        /*
         * Every actual submission counts as an attempt.
         */
        progress.setAttemptCount(
                progress.getAttemptCount() + 1
        );

        boolean correct =
                matchesAnswer(
                        request.getAnswer(),
                        question.getAnswer()
                );

        progress.setCorrect(correct);

        questionProgressRepository.save(progress);

        if (correct) {

            return buildResponse(
                    session,
                    progress,
                    true,
                    "Correct answer!",
                    question.getExplanation()
            );
        }

        return buildResponse(
                session,
                progress,
                false,
                "Incorrect answer. Try again.",
                null
        );
    }

    private boolean matchesAnswer(
            String submittedAnswer,
            JsonNode expectedAnswer
    ) {

        if (submittedAnswer == null ||
                expectedAnswer == null) {

            return false;
        }

        String submitted =
                normalize(submittedAnswer);

        /*
         * Single accepted answer.
         */
        if (expectedAnswer.isTextual()) {

            return submitted.equals(
                    normalize(
                            expectedAnswer.asText()
                    )
            );
        }

        /*
         * Multiple accepted answers.
         */
        if (expectedAnswer.isArray()) {

            for (JsonNode answer : expectedAnswer) {

                if (answer.isTextual() &&
                        submitted.equals(
                                normalize(answer.asText())
                        )) {

                    return true;
                }
            }
        }

        return false;
    }

    private String normalize(String value) {

        return value
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase();
    }

    private VerificationResponse buildResponse(
            GameSession session,
            QuestionProgress progress,
            boolean correct,
            String message,
            String explanation
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

        /*
         * Determine completed rounds.
         *
         * Normal rounds use "questions".
         * Round 10 uses "stages".
         */
        List<Integer> completedRounds =
                quizDataService.getRounds()
                        .stream()
                        .filter(round -> {

                            List<QuizQuestion> roundQuestions =
                                    getRoundQuestions(round);

                            return roundQuestions != null &&
                                    !roundQuestions.isEmpty() &&
                                    roundQuestions.stream()
                                            .allMatch(question ->
                                                    completedQuestionIds
                                                            .contains(
                                                                    question.getId()
                                                            )
                                            );
                        })
                        .map(QuizRound::getId)
                        .toList();

        return new VerificationResponse(
                true,
                correct,
                progress.getQuestionId(),
                message,
                explanation,
                session.getTotalActiveTimeMs(),
                completedQuestionIds,
                completedRounds
        );
    }

    private List<QuizQuestion> getRoundQuestions(
            QuizRound round
    ) {

        if (round.getQuestions() != null &&
                !round.getQuestions().isEmpty()) {

            return round.getQuestions();
        }

        if (round.getStages() != null &&
                !round.getStages().isEmpty()) {

            return round.getStages();
        }

        return List.of();
    }
}