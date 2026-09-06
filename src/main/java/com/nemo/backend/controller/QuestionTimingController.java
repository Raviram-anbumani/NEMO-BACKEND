package com.nemo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.QuestionTimingResponse;
import com.nemo.backend.service.QuestionTimingService;

@RestController
@RequestMapping("/api/sessions/{sessionId}/questions/{questionId}")
public class QuestionTimingController {

    private final QuestionTimingService questionTimingService;

    public QuestionTimingController(
            QuestionTimingService questionTimingService
    ) {
        this.questionTimingService = questionTimingService;
    }

    @PostMapping("/start")
    public ResponseEntity<QuestionTimingResponse> startQuestion(
            @PathVariable String sessionId,
            @PathVariable String questionId
    ) {

        return ResponseEntity.ok(
                questionTimingService.startQuestion(
                        sessionId,
                        questionId
                )
        );
    }

    @PostMapping("/stop")
    public ResponseEntity<QuestionTimingResponse> stopQuestion(
            @PathVariable String sessionId,
            @PathVariable String questionId,
            @RequestBody(required = false) Object ignoredBody
    ) {

        return ResponseEntity.ok(
                questionTimingService.stopQuestion(
                        sessionId,
                        questionId
                )
        );
    }
}