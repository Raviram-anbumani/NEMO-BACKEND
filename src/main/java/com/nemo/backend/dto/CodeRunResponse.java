package com.nemo.backend.dto;

import java.util.List;

import com.nemo.backend.service.Judge0Service.VisibleTestResult;

public class CodeRunResponse {

    private boolean success;
    private String questionId;
    private String message;
    private List<VisibleTestResult> tests;

    public CodeRunResponse(
            boolean success,
            String questionId,
            String message,
            List<VisibleTestResult> tests
    ) {
        this.success = success;
        this.questionId = questionId;
        this.message = message;
        this.tests = tests;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getMessage() {
        return message;
    }

    public List<VisibleTestResult> getTests() {
        return tests;
    }
}