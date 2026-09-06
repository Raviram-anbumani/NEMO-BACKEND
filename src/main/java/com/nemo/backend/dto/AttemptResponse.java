package com.nemo.backend.dto;

public class AttemptResponse {

    private String questionId;
    private int attemptCount;
    private boolean correct;
    private long timeTakenMs;

    public AttemptResponse(
            String questionId,
            int attemptCount,
            boolean correct,
            long timeTakenMs
    ) {
        this.questionId = questionId;
        this.attemptCount = attemptCount;
        this.correct = correct;
        this.timeTakenMs = timeTakenMs;
    }

    public String getQuestionId() {
        return questionId;
    }

    public int getAttemptCount() {
        return attemptCount;
    }

    public boolean isCorrect() {
        return correct;
    }

    public long getTimeTakenMs() {
        return timeTakenMs;
    }
}
