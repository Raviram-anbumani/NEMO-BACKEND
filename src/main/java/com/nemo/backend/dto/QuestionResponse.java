package com.nemo.backend.dto;

import java.util.List;

import com.nemo.backend.model.quiz.QuizOption;

import tools.jackson.databind.JsonNode;

public class QuestionResponse {

    private String id;
    private Integer roundId;
    private Integer stageNumber;
    private String title;
    private String category;
    private String difficulty;
    private String type;
    private String prompt;
    private String codeSnippet;
    private String placeholder;
    private List<QuizOption> options;
    private String functionName;
    private String protocolOutputInstruction;
    private List<JsonNode> sampleTests;

    public QuestionResponse(
            String id,
            Integer roundId,
            Integer stageNumber,
            String title,
            String category,
            String difficulty,
            String type,
            String prompt,
            String codeSnippet,
            String placeholder,
            List<QuizOption> options,
            String functionName,
            String protocolOutputInstruction,
            List<JsonNode> sampleTests
    ) {
        this.id = id;
        this.roundId = roundId;
        this.stageNumber = stageNumber;
        this.title = title;
        this.category = category;
        this.difficulty = difficulty;
        this.type = type;
        this.prompt = prompt;
        this.codeSnippet = codeSnippet;
        this.placeholder = placeholder;
        this.options = options;
        this.functionName = functionName;
        this.protocolOutputInstruction = protocolOutputInstruction;
        this.sampleTests = sampleTests;
    }

    public String getId() {
        return id;
    }

    public Integer getRoundId() {
        return roundId;
    }

    public Integer getStageNumber() {
        return stageNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getCategory() {
        return category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public String getType() {
        return type;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public List<QuizOption> getOptions() {
        return options;
    }

    public String getFunctionName() {
        return functionName;
    }

    public String getProtocolOutputInstruction() {
        return protocolOutputInstruction;
    }

    public List<JsonNode> getSampleTests() {
        return sampleTests;
    }
}