package com.nemo.backend.service;

import java.io.IOException;
import java.util.List;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.nemo.backend.model.quiz.QuizData;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.model.quiz.QuizRound;

import tools.jackson.databind.ObjectMapper;

@Service
public class QuizDataService {

    private final QuizData quizData;

    public QuizDataService(ObjectMapper objectMapper) throws IOException {

        ClassPathResource resource
                = new ClassPathResource("data/quiz-data.json");

        this.quizData
                = objectMapper.readValue(resource.getInputStream(), QuizData.class);
    }

    public QuizData getQuizData() {
        return quizData;
    }

    public List<QuizRound> getRounds() {
        return quizData.getRounds();
    }

    public QuizRound getRound(int roundId) {

        return quizData.getRounds()
                .stream()
                .filter(round -> round.getId() == roundId)
                .findFirst()
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Round not found: " + roundId
                )
                );
    }

    public QuizQuestion getQuestion(String questionId) {

        return quizData.getRounds()
                .stream()
                .flatMap(round -> {

                    if (round.getQuestions() != null) {
                        return round.getQuestions().stream();
                    }

                    if (round.getStages() != null) {
                        return round.getStages().stream();
                    }

                    return java.util.stream.Stream.empty();
                })
                .filter(question
                        -> question.getId().equals(questionId)
                )
                .findFirst()
                .orElseThrow(()
                        -> new IllegalArgumentException(
                        "Question not found: " + questionId
                )
                );
    }
}
