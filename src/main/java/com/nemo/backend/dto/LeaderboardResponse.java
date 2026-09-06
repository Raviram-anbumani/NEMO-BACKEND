package com.nemo.backend.dto;

import java.time.LocalDateTime;

public class LeaderboardResponse {

    private int rank;
    private String name;
    private long totalActiveMs;
    private LocalDateTime completedAt;

    public LeaderboardResponse(
            int rank,
            String name,
            long totalActiveMs,
            LocalDateTime completedAt
    ) {
        this.rank = rank;
        this.name = name;
        this.totalActiveMs = totalActiveMs;
        this.completedAt = completedAt;
    }

    public int getRank() {
        return rank;
    }

    public String getName() {
        return name;
    }

    public long getTotalActiveMs() {
        return totalActiveMs;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}