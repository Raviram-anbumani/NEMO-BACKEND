package com.nemo.backend.dto;

import java.time.LocalDateTime;

public class LeaderboardResponse {

    private int rank;
    private String name;
    private String email;

    private int completedRounds;
    private int totalRounds;
    private boolean fullyCompleted;

    private long totalActiveMs;
    private LocalDateTime completedAt;

    public LeaderboardResponse(
            int rank,
            String name,
            String email,
            int completedRounds,
            int totalRounds,
            boolean fullyCompleted,
            long totalActiveMs,
            LocalDateTime completedAt
    ) {
        this.rank = rank;
        this.name = name;
        this.email = email;
        this.completedRounds = completedRounds;
        this.totalRounds = totalRounds;
        this.fullyCompleted = fullyCompleted;
        this.totalActiveMs = totalActiveMs;
        this.completedAt = completedAt;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public int getCompletedRounds() {
        return completedRounds;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public boolean isFullyCompleted() {
        return fullyCompleted;
    }

    public long getTotalActiveMs() {
        return totalActiveMs;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}