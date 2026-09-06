package com.nemo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.CodeVerificationRequest;
import com.nemo.backend.dto.VerificationResponse;
import com.nemo.backend.service.CodeVerificationService;

@RestController
@RequestMapping("/api/verify")
public class CodeVerificationController {

    private final CodeVerificationService codeVerificationService;

    public CodeVerificationController(
            CodeVerificationService codeVerificationService
    ) {
        this.codeVerificationService = codeVerificationService;
    }

    @PostMapping("/code")
    public ResponseEntity<VerificationResponse> verifyCode(
            @RequestBody CodeVerificationRequest request
    ) {

        VerificationResponse response =
                codeVerificationService.verify(request);

        return ResponseEntity.ok(response);
    }
}