package com.nemo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.RoundResponse;
import com.nemo.backend.model.quiz.QuizRound;
import com.nemo.backend.service.QuizDataService;

@RestController
@RequestMapping("/api/rounds")
public class RoundController {

    private final QuizDataService quizDataService;

    public RoundController(QuizDataService quizDataService) {
        this.quizDataService = quizDataService;
    }

    @GetMapping
    public ResponseEntity<List<RoundResponse>> getRounds() {

        List<RoundResponse> rounds =
                quizDataService.getRounds()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return ResponseEntity.ok(rounds);
    }

    private RoundResponse toResponse(QuizRound round) {

        return new RoundResponse(
                round.getId(),
                round.getTitle(),
                round.getType(),
                round.getDepthMeters(),
                round.getEnvironment(),
                round.getInstruction()
        );
    }
}