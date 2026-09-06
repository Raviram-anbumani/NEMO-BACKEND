package com.nemo.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.Round10VerificationRequest;
import com.nemo.backend.dto.VerificationResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.QuestionProgressRepository;

@Service
public class Round10VerificationService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionProgressRepository questionProgressRepository;
    private final QuizDataService quizDataService;

    public Round10VerificationService(
            GameSessionRepository gameSessionRepository,
            QuestionProgressRepository questionProgressRepository,
            QuizDataService quizDataService
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionProgressRepository = questionProgressRepository;
        this.quizDataService = quizDataService;
    }

    @Transactional
    public VerificationResponse verify(
            int stage,
            Round10VerificationRequest request
    ) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Request cannot be empty"
            );
        }

        if (stage < 1 || stage > 5) {
            throw new IllegalArgumentException(
                    "Invalid Round 10 stage"
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

        if (request.getAnswer() == null ||
                request.getAnswer().isBlank()) {

            throw new IllegalArgumentException(
                    "Answer is required"
            );
        }

        /*
         * Stage 1 requires round 10 stage = 0.
         * Stage 2 requires stage 1 completed, etc.
         */
        if (session.getRound10Stage() != stage - 1) {
            throw new IllegalArgumentException(
                    "Round 10 stage is currently locked"
            );
        }

        String questionId = "Q" + (19 + stage);

        QuizQuestion question =
                quizDataService.getQuestion(questionId);

        QuestionProgress progress =
                questionProgressRepository
                        .findBySessionAndQuestionId(
                                session,
                                questionId
                        )
                        .orElseGet(() -> {

                            QuestionProgress newProgress =
                                    new QuestionProgress();

                            newProgress.setSession(session);
                            newProgress.setQuestionId(
                                    questionId
                            );

                            return newProgress;
                        });

        /*
         * A correctly completed stage cannot be submitted again.
         */
        if (Boolean.TRUE.equals(progress.getCorrect())) {

            return buildResponse(
                    session,
                    progress,
                    true,
                    "Stage already completed.",
                    question.getExplanation()
            );
        }

        progress.setAttemptCount(
                progress.getAttemptCount() + 1
        );

        String submittedAnswer =
                request.getAnswer()
                        .trim()
                        .toUpperCase();

        String expectedAnswer =
                getExpectedAnswer(stage);

        boolean correct =
                submittedAnswer.equals(expectedAnswer);

        progress.setCorrect(correct);

        if (correct) {
            updateRound10State(
                    session,
                    stage
            );
        }

        questionProgressRepository.save(progress);
        gameSessionRepository.save(session);

        if (correct) {

            String message;

            if (stage == 5) {
                message =
                        "Correct! Nemo Rescue Protocol completed.";
            } else {
                message =
                        "Correct! Stage " + stage
                                + " completed.";
            }

            return buildResponse(
                    session,
                    progress,
                    true,
                    message,
                    question.getExplanation()
            );
        }

        return buildResponse(
                session,
                progress,
                false,
                "Incorrect. Try again.",
                null
        );
    }

    private String getExpectedAnswer(int stage) {

        return switch (stage) {

            case 1 -> "A";

            case 2 -> "D";

            case 3 -> "C";

            case 4 -> "B";

            case 5 -> "A";

            default ->
                    throw new IllegalArgumentException(
                            "Invalid Round 10 stage"
                    );
        };
    }

    private void updateRound10State(
            GameSession session,
            int stage
    ) {

        switch (stage) {

            case 1 -> {
                session.setRound10N(5);
                session.setRound10Stage(1);
            }

            case 2 -> {
                session.setRound10V(10);
                session.setRound10Stage(2);
            }

            case 3 -> {
                session.setRound10K(4);
                session.setRound10Stage(3);
            }

            case 4 -> {
                session.setRound10L("Z");
                session.setRound10Stage(4);
            }

            case 5 -> {
                session.setRound10Stage(5);
            }

            default ->
                    throw new IllegalArgumentException(
                            "Invalid Round 10 stage"
                    );
        }
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

        List<Integer> completedRounds =
                quizDataService.getRounds()
                        .stream()
                        .filter(round ->
                                getRoundQuestions(round)
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

    private List<QuizQuestion> getRoundQuestions(
            com.nemo.backend.model.quiz.QuizRound round
    ) {

        if (round.getQuestions() != null) {
            return round.getQuestions();
        }

        if (round.getStages() != null) {
            return round.getStages();
        }

        return List.of();
    }
}