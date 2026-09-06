package com.nemo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.QuestionResponse;
import com.nemo.backend.dto.RoundDetailsResponse;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.model.quiz.QuizRound;
import com.nemo.backend.service.QuizDataService;

@RestController
@RequestMapping("/api/rounds")
public class QuestionController {

    private final QuizDataService quizDataService;

    public QuestionController(QuizDataService quizDataService) {
        this.quizDataService = quizDataService;
    }

    @GetMapping("/{roundId}/questions")
    public ResponseEntity<RoundDetailsResponse> getQuestions(
            @PathVariable int roundId
    ) {

        QuizRound round =
                quizDataService.getRound(roundId);

        List<QuizQuestion> sourceQuestions;

        /*
         * Rounds 1-9 use "questions".
         * Round 10 uses "stages".
         */
        if (round.getQuestions() != null &&
                !round.getQuestions().isEmpty()) {

            sourceQuestions = round.getQuestions();

        } else {
            sourceQuestions = round.getStages();
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
                question.getProtocolOutputInstruction()
        );
    }
}