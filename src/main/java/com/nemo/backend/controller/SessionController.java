package com.nemo.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.CreateSessionRequest;
import com.nemo.backend.dto.SessionResponse;
import com.nemo.backend.service.SessionService;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @RequestBody CreateSessionRequest request
    ) {
        SessionResponse response =
                sessionService.createSession(request);

        return ResponseEntity.ok(response);
    }
}