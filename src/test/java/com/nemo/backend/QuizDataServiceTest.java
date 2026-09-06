package com.nemo.backend;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.model.quiz.QuizRound;
import com.nemo.backend.service.QuizDataService;

@SpringBootTest
class QuizDataServiceTest {

    @Autowired
    private QuizDataService quizDataService;

    @Test
    void quizDataShouldBeLoadedCorrectly() {

        // Verify rounds
        List<QuizRound> rounds = quizDataService.getRounds();

        assertEquals(10, rounds.size());

        // Verify total questions
        long totalQuestions = rounds.stream()
                .mapToLong(round -> round.getQuestions().size())
                .sum();

        assertEquals(24, totalQuestions);

        // Verify Q1 -> Q24
        for (int i = 1; i <= 24; i++) {
            String questionId = "Q" + i;

            QuizQuestion question =
                    quizDataService.getQuestion(questionId);

            assertNotNull(question);
            assertEquals(questionId, question.getId());
        }

        // Verify Round 10
        QuizRound round10 = quizDataService.getRound(10);

        assertEquals(5, round10.getQuestions().size());

        for (int i = 0; i < 5; i++) {
            QuizQuestion question = round10.getQuestions().get(i);

            assertEquals("Q" + (20 + i), question.getId());
            assertEquals(i + 1, question.getStageNumber());
        }
    }
}