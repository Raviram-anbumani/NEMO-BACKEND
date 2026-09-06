package com.nemo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.CompletionResponse;
import com.nemo.backend.service.CompletionService;

@RestController
@RequestMapping("/api/sessions")
public class CompletionController {

    private final CompletionService completionService;

    public CompletionController(CompletionService completionService) {
        this.completionService = completionService;
    }

    @PostMapping("/{sessionId}/complete")
    public ResponseEntity<CompletionResponse> completeSession(
            @PathVariable String sessionId
    ) {

        CompletionResponse response =
                completionService.completeSession(sessionId);

        return ResponseEntity.ok(response);
    }
}