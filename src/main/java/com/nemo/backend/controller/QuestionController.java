package com.nemo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.QuestionResponse;
import com.nemo.backend.dto.RoundDetailsResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.model.quiz.QuizRound;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.service.QuizDataService;
import com.nemo.backend.service.QuizProgressionService;

@RestController
@RequestMapping("/api/rounds")
public class QuestionController {

    private final QuizDataService quizDataService;
    private final QuizProgressionService quizProgressionService;
    private final GameSessionRepository gameSessionRepository;

    public QuestionController(
            QuizDataService quizDataService,
            QuizProgressionService quizProgressionService,
            GameSessionRepository gameSessionRepository
    ) {
        this.quizDataService = quizDataService;
        this.quizProgressionService = quizProgressionService;
        this.gameSessionRepository = gameSessionRepository;
    }

    @GetMapping("/{roundId}/questions")
    public ResponseEntity<RoundDetailsResponse> getQuestions(
            @PathVariable int roundId,
            @RequestParam String sessionId
    ) {

        if (sessionId == null || sessionId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        GameSession session =
                gameSessionRepository.findById(sessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Session not found"
                                )
                        );

        if (session.getStatus() != GameSession.Status.ACTIVE) {
            return ResponseEntity.status(403).build();
        }

        QuizRound round =
                quizDataService.getRound(roundId);

        /*
         * Round 10 is a special multi-stage round.
         *
         * Only the currently unlocked stage should be
         * returned to the participant.
         */
        if (roundId == 10) {

            int currentStage =
                    session.getRound10Stage() == null
                            ? 0
                            : session.getRound10Stage();

            /*
             * Stage 1 requires all previous rounds to be
             * completed.
             */
            for (int previousRound = 1;
                    previousRound <= 9;
                    previousRound++) {

                if (!quizProgressionService.isRoundCompleted(
                        session,
                        previousRound
                )) {
                    return ResponseEntity.status(403).build();
                }
            }

            int nextStage = currentStage + 1;

            /*
             * Round 10 is already completely finished.
             */
            if (nextStage > 5) {
                return ResponseEntity.status(403).build();
            }

            List<QuizQuestion> stages;

                if (round.getQuestions() != null &&
                        !round.getQuestions().isEmpty()) {

                stages = round.getQuestions();

                } else if (round.getStages() != null &&
                        !round.getStages().isEmpty()) {

                stages = round.getStages();

                } else {

                stages = List.of();
                }

            final int stageToReturn = nextStage;

            List<QuizQuestion> sourceQuestions =
                    stages.stream()
                            .filter(question ->
                                    question.getStageNumber() ==
                                            stageToReturn
                            )
                            .toList();

            if (sourceQuestions.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            List<QuestionResponse> questions =
                    sourceQuestions.stream()
                            .map(this::toResponse)
                            .toList();

            RoundDetailsResponse.RoundDetails roundDetails =
                    new RoundDetailsResponse.RoundDetails(
                            round.getId(),
                            round.getTitle(),
                            round.getType(),
                            round.getDepthMeters(),
                            round.getEnvironment(),
                            round.getInstruction(),
                            questions
                    );

            return ResponseEntity.ok(
                    new RoundDetailsResponse(roundDetails)
            );
        }

        /*
         * Normal rounds 1-9.
         */
        List<QuizQuestion> sourceQuestions;

        if (round.getQuestions() != null &&
                !round.getQuestions().isEmpty()) {

            sourceQuestions = round.getQuestions();

        } else if (round.getStages() != null &&
                !round.getStages().isEmpty()) {

            sourceQuestions = round.getStages();

        } else {

            sourceQuestions = List.of();
        }

        /*
         * Backend-authoritative progression check.
         */
        boolean roundUnlocked =
                sourceQuestions.stream()
                        .allMatch(question ->
                                quizProgressionService
                                        .isQuestionUnlocked(
                                                session,
                                                question
                                        )
                        );

        if (!roundUnlocked) {
            return ResponseEntity.status(403).build();
        }

        List<QuestionResponse> questions =
                sourceQuestions.stream()
                        .map(this::toResponse)
                        .toList();

        RoundDetailsResponse.RoundDetails roundDetails =
                new RoundDetailsResponse.RoundDetails(
                        round.getId(),
                        round.getTitle(),
                        round.getType(),
                        round.getDepthMeters(),
                        round.getEnvironment(),
                        round.getInstruction(),
                        questions
                );

        return ResponseEntity.ok(
                new RoundDetailsResponse(roundDetails)
        );
    }

    private QuestionResponse toResponse(
            QuizQuestion question
    ) {

        return new QuestionResponse(
                question.getId(),
                question.getRoundId(),
                question.getStageNumber(),
                question.getTitle(),
                question.getCategory(),
                question.getDifficulty(),
                question.getType(),
                question.getPrompt(),
                question.getCodeSnippet(),
                question.getPlaceholder(),
                question.getOptions(),
                question.getFunctionName(),
                question.getProtocolOutputInstruction(),
                question.getSampleTests()
        );
    }
}