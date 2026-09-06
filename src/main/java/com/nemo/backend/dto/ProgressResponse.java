package com.nemo.backend.dto;

import java.util.List;

public class ProgressResponse {

    private String sessionId;
    private String name;
    private String email;

    private long totalActiveMs;

    private List<String> completedQuestionIds;
    private List<Integer> completedRounds;

    private int unlockedRoundId;

    public ProgressResponse(
            String sessionId,
            String name,
            String email,
            long totalActiveMs,
            List<String> completedQuestionIds,
            List<Integer> completedRounds,
            int unlockedRoundId
    ) {
        this.sessionId = sessionId;
        this.name = name;
        this.email = email;
        this.totalActiveMs = totalActiveMs;
        this.completedQuestionIds = completedQuestionIds;
        this.completedRounds = completedRounds;
        this.unlockedRoundId = unlockedRoundId;
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

    public List<String> getCompletedQuestionIds() {
        return completedQuestionIds;
    }

    public List<Integer> getCompletedRounds() {
        return completedRounds;
    }

    public int getUnlockedRoundId() {
        return unlockedRoundId;
    }
}