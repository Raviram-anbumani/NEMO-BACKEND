package com.nemo.backend.dto;

import java.util.List;

public class VerificationResponse {

    private boolean success;
    private boolean correct;
    private String questionId;
    private String message;
    private String explanation;

    private long totalActiveMs;

    private List<String> completedQuestionIds;
    private List<Integer> completedRounds;

    public VerificationResponse(
            boolean success,
            boolean correct,
            String questionId,
            String message,
            String explanation,
            long totalActiveMs,
            List<String> completedQuestionIds,
            List<Integer> completedRounds
    ) {
        this.success = success;
        this.correct = correct;
        this.questionId = questionId;
        this.message = message;
        this.explanation = explanation;
        this.totalActiveMs = totalActiveMs;
        this.completedQuestionIds = completedQuestionIds;
        this.completedRounds = completedRounds;
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
}