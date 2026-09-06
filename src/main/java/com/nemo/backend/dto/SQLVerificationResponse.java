package com.nemo.backend.dto;

import java.util.List;
import java.util.Map;

public class SQLVerificationResponse {

    private boolean success;
    private boolean correct;
    private String questionId;
    private String message;
    private String explanation;

    private long totalActiveMs;

    private List<String> completedQuestionIds;
    private List<Integer> completedRounds;

    private List<Map<String, Object>> rows;

    public SQLVerificationResponse(
            boolean success,
            boolean correct,
            String questionId,
            String message,
            String explanation,
            long totalActiveMs,
            List<String> completedQuestionIds,
            List<Integer> completedRounds,
            List<Map<String, Object>> rows
    ) {
        this.success = success;
        this.correct = correct;
        this.questionId = questionId;
        this.message = message;
        this.explanation = explanation;
        this.totalActiveMs = totalActiveMs;
        this.completedQuestionIds = completedQuestionIds;
        this.completedRounds = completedRounds;
        this.rows = rows;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getMessage() {
        return message;
    }

    public String getExplanation() {
        return explanation;
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

    public List<Map<String, Object>> getRows() {
        return rows;
    }
}