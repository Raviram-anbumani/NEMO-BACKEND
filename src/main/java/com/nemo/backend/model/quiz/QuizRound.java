package com.nemo.backend.model.quiz;

import java.util.List;

public class QuizRound {

    private int id;
    private String title;
    private String type;
    private int depthMeters;
    private String environment;
    private String instruction;
    private List<QuizQuestion> questions;
    private List<QuizQuestion> stages;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDepthMeters() {
        return depthMeters;
    }

    public void setDepthMeters(int depthMeters) {
        this.depthMeters = depthMeters;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public List<QuizQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuizQuestion> questions) {
        this.questions = questions;
    }

    public List<QuizQuestion> getStages() {
        return stages;
    }

    public void setStages(List<QuizQuestion> stages) {
        this.stages = stages;
    }
}
