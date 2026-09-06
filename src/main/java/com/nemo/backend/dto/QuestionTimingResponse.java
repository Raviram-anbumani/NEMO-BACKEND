package com.nemo.backend.dto;

public class QuestionTimingResponse {

    private boolean success;
    private String sessionId;
    private String questionId;
    private long activeMs;
    private long totalActiveMs;
    private boolean active;

    public QuestionTimingResponse(
            boolean success,
            String sessionId,
            String questionId,
            long activeMs,
            long totalActiveMs,
            boolean active
    ) {
        this.success = success;
        this.sessionId = sessionId;
        this.questionId = questionId;
        this.activeMs = activeMs;
        this.totalActiveMs = totalActiveMs;
        this.active = active;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public long getActiveMs() {
        return activeMs;
    }

    public long getTotalActiveMs() {
        return totalActiveMs;
    }

    public boolean isActive() {
        return active;
    }
}