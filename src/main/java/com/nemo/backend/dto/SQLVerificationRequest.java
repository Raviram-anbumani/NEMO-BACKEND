package com.nemo.backend.dto;

public class SQLVerificationRequest {

    private String sessionId;
    private String questionId;
    private String sqlQuery;
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

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public Long getClientElapsedMs() {
        return clientElapsedMs;
    }

    public void setClientElapsedMs(Long clientElapsedMs) {
        this.clientElapsedMs = clientElapsedMs;
    }
}