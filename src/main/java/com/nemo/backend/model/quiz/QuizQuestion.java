package com.nemo.backend.model.quiz;
import java.util.List;

import tools.jackson.databind.JsonNode;

public class QuizQuestion {

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

    private JsonNode answer;
    private String explanation;

    private List<JsonNode> hiddenTests;

    private String functionName;
    private String protocolOutputInstruction;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getRoundId() {
        return roundId;
    }

    public void setRoundId(Integer roundId) {
        this.roundId = roundId;
    }

    public Integer getStageNumber() {
        return stageNumber;
    }

    public void setStageNumber(Integer stageNumber) {
        this.stageNumber = stageNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getCodeSnippet() {
        return codeSnippet;
    }

    public void setCodeSnippet(String codeSnippet) {
        this.codeSnippet = codeSnippet;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public List<QuizOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuizOption> options) {
        this.options = options;
    }

    public JsonNode getAnswer() {
        return answer;
    }

    public void setAnswer(JsonNode answer) {
        this.answer = answer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<JsonNode> getHiddenTests() {
        return hiddenTests;
    }

    public void setHiddenTests(List<JsonNode> hiddenTests) {
        this.hiddenTests = hiddenTests;
    }

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public String getProtocolOutputInstruction() {
        return protocolOutputInstruction;
    }

    public void setProtocolOutputInstruction(String protocolOutputInstruction) {
        this.protocolOutputInstruction = protocolOutputInstruction;
    }
}