package com.nemo.backend.dto;

import java.time.LocalDateTime;

public class CompletionResponse {

    private boolean success;
    private String sessionId;
    private String name;
    private String email;
    private long totalActiveMs;
    private LocalDateTime completedAt;

    public CompletionResponse(
            boolean success,
            String sessionId,
            String name,
            String email,
            long totalActiveMs,
            LocalDateTime completedAt
    ) {
        this.success = success;
        this.sessionId = sessionId;
        this.name = name;
        this.email = email;
        this.totalActiveMs = totalActiveMs;
        this.completedAt = completedAt;
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

    public long getTotalActiveMs() {
        return totalActiveMs;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}