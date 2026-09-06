package com.nemo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.MCQVerificationRequest;
import com.nemo.backend.dto.RiddleVerificationRequest;
import com.nemo.backend.dto.SQLVerificationRequest;
import com.nemo.backend.dto.SQLVerificationResponse;
import com.nemo.backend.dto.VerificationResponse;
import com.nemo.backend.service.MCQVerificationService;
import com.nemo.backend.service.RiddleVerificationService;
import com.nemo.backend.service.SQLVerificationService;

@RestController
@RequestMapping("/api/verify")
public class VerificationController {

    private final MCQVerificationService mcqVerificationService;
    private final RiddleVerificationService riddleVerificationService;
    private final SQLVerificationService sqlVerificationService;

    public VerificationController(
            MCQVerificationService mcqVerificationService,
            RiddleVerificationService riddleVerificationService,
            SQLVerificationService sqlVerificationService
    ) {
        this.mcqVerificationService = mcqVerificationService;
        this.riddleVerificationService = riddleVerificationService;
        this.sqlVerificationService = sqlVerificationService;
    }

    @PostMapping("/mcq")
    public ResponseEntity<VerificationResponse> verifyMCQ(
            @RequestBody MCQVerificationRequest request
    ) {

        return ResponseEntity.ok(
                mcqVerificationService.verify(request)
        );
    }

    @PostMapping("/riddle")
    public ResponseEntity<VerificationResponse> verifyRiddle(
            @RequestBody RiddleVerificationRequest request
    ) {

        return ResponseEntity.ok(
                riddleVerificationService.verify(request)
        );
    }

    @PostMapping("/sql")
    public ResponseEntity<SQLVerificationResponse> verifySQL(
            @RequestBody SQLVerificationRequest request
    ) {

        return ResponseEntity.ok(
                sqlVerificationService.verify(request)
        );
    }
}
