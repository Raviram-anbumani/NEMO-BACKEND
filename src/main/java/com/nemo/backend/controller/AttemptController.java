package com.nemo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.AttemptResponse;
import com.nemo.backend.service.AttemptService;

@RestController
@RequestMapping("/api/sessions")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @GetMapping("/{sessionId}/attempts")
    public ResponseEntity<List<AttemptResponse>> getAttempts(
            @PathVariable String sessionId
    ) {

        return ResponseEntity.ok(
                attemptService.getAttempts(sessionId)
        );
    }
}