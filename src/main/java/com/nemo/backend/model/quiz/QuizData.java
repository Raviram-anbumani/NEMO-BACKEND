package com.nemo.backend.model.quiz;

import java.util.List;

public class QuizData {

    private Meta meta;
    private List<QuizRound> rounds;

    public Meta getMeta() {
        return meta;
    }

    public void setMeta(Meta meta) {
        this.meta = meta;
    }

    public List<QuizRound> getRounds() {
        return rounds;
    }

    public void setRounds(List<QuizRound> rounds) {
        this.rounds = rounds;
    }

    public static class Meta {

        private String title;
        private String subtitle;
        private int totalRounds;
        private int totalQuestions;
        private String source;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public int getTotalRounds() {
            return totalRounds;
        }

        public void setTotalRounds(int totalRounds) {
            this.totalRounds = totalRounds;
        }

        public int getTotalQuestions() {
            return totalQuestions;
        }

        public void setTotalQuestions(int totalQuestions) {
            this.totalQuestions = totalQuestions;
        }

        public String getSource() {
            return source;
        }

        public void setSource(String source) {
            this.source = source;
        }
    }
}