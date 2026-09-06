package com.nemo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.Round10VerificationRequest;
import com.nemo.backend.dto.VerificationResponse;
import com.nemo.backend.service.Round10VerificationService;

@RestController
@RequestMapping("/api/round10")
public class Round10VerificationController {

    private final Round10VerificationService round10VerificationService;

    public Round10VerificationController(
            Round10VerificationService round10VerificationService
    ) {
        this.round10VerificationService =
                round10VerificationService;
    }

    @PostMapping("/stage/{stage}")
    public ResponseEntity<VerificationResponse> verifyStage(
            @PathVariable int stage,
            @RequestBody Round10VerificationRequest request
    ) {

        VerificationResponse response =
                round10VerificationService.verify(
                        stage,
                        request
                );

        return ResponseEntity.ok(response);
    }
}