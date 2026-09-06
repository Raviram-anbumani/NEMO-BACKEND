package com.nemo.backend.dto;

import java.util.List;

public class RoundDetailsResponse {

    private RoundDetails round;

    public RoundDetailsResponse(RoundDetails round) {
        this.round = round;
    }

    public RoundDetails getRound() {
        return round;
    }

    public static class RoundDetails {

        private int id;
        private String title;
        private String type;
        private int depthMeters;
        private String environment;
        private String instruction;
        private List<QuestionResponse> questions;

        public RoundDetails(
                int id,
                String title,
                String type,
                int depthMeters,
                String environment,
                String instruction,
                List<QuestionResponse> questions
        ) {
            this.id = id;
            this.title = title;
            this.type = type;
            this.depthMeters = depthMeters;
            this.environment = environment;
            this.instruction = instruction;
            this.questions = questions;
        }

        public int getId() {
            return id;
        }

        public String getTitle() {
            return title;
        }

        public String getType() {
            return type;
        }

        public int getDepthMeters() {
            return depthMeters;
        }

        public String getEnvironment() {
            return environment;
        }

        public String getInstruction() {
            return instruction;
        }

        public List<QuestionResponse> getQuestions() {
            return questions;
        }
    }
}