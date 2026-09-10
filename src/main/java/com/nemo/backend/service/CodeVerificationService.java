package com.nemo.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.CodeVerificationRequest;
import com.nemo.backend.dto.VerificationResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.QuestionProgressRepository;
import com.nemo.backend.service.Judge0Service.CodeVerificationResult;

@Service
public class CodeVerificationService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionProgressRepository questionProgressRepository;
    private final QuizDataService quizDataService;
    private final QuizProgressionService quizProgressionService;
    private final Judge0Service judge0Service;

    public CodeVerificationService(
            GameSessionRepository gameSessionRepository,
            QuestionProgressRepository questionProgressRepository,
            QuizDataService quizDataService,
            QuizProgressionService quizProgressionService,
            Judge0Service judge0Service
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionProgressRepository = questionProgressRepository;
        this.quizDataService = quizDataService;
        this.quizProgressionService = quizProgressionService;
        this.judge0Service = judge0Service;
    }

    @Transactional
    public VerificationResponse verify(
            CodeVerificationRequest request
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

        if (request.getCode() == null ||
                request.getCode().isBlank()) {

            throw new IllegalArgumentException(
                    "Code is required"
            );
        }

        QuizQuestion question =
                quizDataService.getQuestion(
                        request.getQuestionId()
                );

        if (!"code".equalsIgnoreCase(question.getType())) {
            throw new IllegalArgumentException(
                    "Question is not a programming question"
            );
        }

        // Backend-enforced sequential progression.
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
         * Once correctly solved, further submissions
         * must not change the question state.
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

        // Every code submission is one attempt.
        progress.setAttemptCount(
                progress.getAttemptCount() + 1
        );

        /*
         * Judge0 executes the submitted code against
         * backend-only hidden tests.
         */
        CodeVerificationResult result =
                judge0Service.verifyCode(
                        question.getId(),
                        request.getCode(),
                        request.getLanguage()
                );

        boolean correct = result.success();

        progress.setCorrect(correct);

        questionProgressRepository.save(progress);

        if (correct) {

            return buildResponse(
                    session,
                    progress,
                    true,
                    "Correct! All hidden tests passed.",
                    question.getExplanation()
            );
        }

        return buildResponse(
                session,
                progress,
                false,
                "Incorrect. Some hidden tests failed. Try again.",
                null
        );
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
                                Boolean.TRUE.equals(p.getCorrect())
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
                        .map(round -> round.getId())
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
}