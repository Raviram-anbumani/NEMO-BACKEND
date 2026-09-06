package com.nemo.backend.dto;

import java.time.LocalDateTime;

public class SessionResponse {

    private boolean success;
    private String sessionId;
    private String name;
    private String email;
    private LocalDateTime startedAt;

    public SessionResponse(
            boolean success,
            String sessionId,
            String name,
            String email,
            LocalDateTime startedAt
    ) {
        this.success = success;
        this.sessionId = sessionId;
        this.name = name;
        this.email = email;
        this.startedAt = startedAt;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }
}