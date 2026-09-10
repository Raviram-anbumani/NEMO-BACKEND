package com.nemo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.CodeRunRequest;
import com.nemo.backend.dto.CodeRunResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.service.Judge0Service;
import com.nemo.backend.service.Judge0Service.VisibleTestResult;

@RestController
@RequestMapping("/api/run")
public class CodeRunController {

    private final GameSessionRepository gameSessionRepository;
    private final Judge0Service judge0Service;

    public CodeRunController(
            GameSessionRepository gameSessionRepository,
            Judge0Service judge0Service
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.judge0Service = judge0Service;
    }

    @PostMapping("/code")
    public ResponseEntity<CodeRunResponse> runCode(
            @RequestBody CodeRunRequest request
    ) {

        if (request == null ||
                request.getSessionId() == null ||
                request.getSessionId().isBlank()) {

            throw new IllegalArgumentException(
                    "Session ID is required"
            );
        }

        GameSession session =
                gameSessionRepository
                        .findById(request.getSessionId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Session not found"
                                )
                        );

        if (session.getStatus() != GameSession.Status.ACTIVE) {
            throw new IllegalArgumentException(
                    "Session is not active"
            );
        }

        if (request.getQuestionId() == null ||
                request.getQuestionId().isBlank()) {

            throw new IllegalArgumentException(
                    "Question ID is required"
            );
        }

        if (request.getCode() == null ||
                request.getCode().isBlank()) {

            throw new IllegalArgumentException(
                    "Code cannot be empty"
            );
        }

        List<VisibleTestResult> tests =
                judge0Service.runVisibleTests(
                        request.getQuestionId(),
                        request.getCode(),
                        request.getLanguage()
                );

        boolean allPassed =
                tests.stream()
                        .allMatch(VisibleTestResult::passed);

        CodeRunResponse response =
                new CodeRunResponse(
                        allPassed,
                        request.getQuestionId(),
                        allPassed
                                ? "All sample test cases passed."
                                : "Some sample test cases failed.",
                        tests
                );

        return ResponseEntity.ok(response);
    }
}