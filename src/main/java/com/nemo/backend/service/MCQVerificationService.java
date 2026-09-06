package com.nemo.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.MCQVerificationRequest;
import com.nemo.backend.dto.VerificationResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.QuestionProgressRepository;

@Service
public class MCQVerificationService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionProgressRepository questionProgressRepository;
    private final QuizDataService quizDataService;
    private final QuizProgressionService quizProgressionService;

    public MCQVerificationService(
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
            MCQVerificationRequest request
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

        if (!"mcq".equalsIgnoreCase(question.getType())) {
            throw new IllegalArgumentException(
                    "Question is not an MCQ"
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
         * Once a question is correctly solved,
         * additional submissions must not change its state.
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

        // Every actual submission counts as an attempt.
        progress.setAttemptCount(
                progress.getAttemptCount() + 1
        );

        String submittedAnswer =
                request.getAnswer() == null
                        ? ""
                        : request.getAnswer()
                                .trim()
                                .toUpperCase();

        String correctAnswer =
                question.getAnswer().asText()
                        .trim()
                        .toUpperCase();

        boolean correct =
                submittedAnswer.equals(correctAnswer);

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
                .filter(round -> {

                    List<QuizQuestion> roundQuestions;

                    if (round.getQuestions() != null &&
                            !round.getQuestions().isEmpty()) {

                        roundQuestions = round.getQuestions();

                    } else {
                        roundQuestions = round.getStages();
                    }

                    return roundQuestions != null &&
                            !roundQuestions.isEmpty() &&
                            roundQuestions.stream()
                                    .allMatch(question ->
                                            completedQuestionIds.contains(
                                                    question.getId()
                                            )
                                    );
                })
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