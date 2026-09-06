package com.nemo.backend.dto;

public class MCQVerificationRequest {

    private String sessionId;
    private String questionId;
    private String answer;

    // Sent by frontend but NOT trusted by backend.
    private Long clientElapsedMs;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Long getClientElapsedMs() {
        return clientElapsedMs;
    }

    public void setClientElapsedMs(Long clientElapsedMs) {
        this.clientElapsedMs = clientElapsedMs;
    }
}