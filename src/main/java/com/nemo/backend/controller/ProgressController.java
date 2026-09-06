package com.nemo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.ProgressResponse;
import com.nemo.backend.service.ProgressService;

@RestController
@RequestMapping("/api/sessions")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @GetMapping("/{sessionId}/progress")
    public ResponseEntity<ProgressResponse> getProgress(
            @PathVariable String sessionId
    ) {

        return ResponseEntity.ok(
                progressService.getProgress(sessionId)
        );
    }
}