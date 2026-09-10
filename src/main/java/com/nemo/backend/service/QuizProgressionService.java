package com.nemo.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.model.quiz.QuizRound;
import com.nemo.backend.repository.QuestionProgressRepository;

@Service
public class QuizProgressionService {

        private final QuestionProgressRepository questionProgressRepository;
        private final QuizDataService quizDataService;

        public QuizProgressionService(
                        QuestionProgressRepository questionProgressRepository,
                        QuizDataService quizDataService) {
                this.questionProgressRepository = questionProgressRepository;
                this.quizDataService = quizDataService;
        }

        public boolean isQuestionUnlocked(
                        GameSession session,
                        QuizQuestion question) {

                int roundId = getQuestionRoundId(question);

                // Round 1 is always unlocked.
                if (roundId == 1) {
                        return true;
                }

                // Every previous round must be completed.
                for (int previousRound = 1; previousRound < roundId; previousRound++) {

                        if (!isRoundCompleted(session, previousRound)) {
                                return false;
                        }
                }

                return true;
        }

        public boolean isRoundCompleted(
                        GameSession session,
                        int roundId) {

                QuizRound round = quizDataService.getRound(roundId);

                List<QuestionProgress> progressList = questionProgressRepository.findBySession(session);

                List<QuizQuestion> roundQuestions = getRoundQuestions(round);

                return roundQuestions
                                .stream()
                                .allMatch(question -> progressList.stream()
                                                .anyMatch(progress -> progress.getQuestionId()
                                                                .equals(question.getId())
                                                                && Boolean.TRUE.equals(
                                                                                progress.getCorrect())));
        }

        private int getQuestionRoundId(
                        QuizQuestion question) {

                if (question.getRoundId() != null) {
                        return question.getRoundId();
                }

                return quizDataService.getRounds()
                                .stream()
                                .filter(round -> getRoundQuestions(round)
                                                .stream()
                                                .anyMatch(q -> q.getId().equals(
                                                                question.getId())))
                                .map(QuizRound::getId)
                                .findFirst()
                                .orElseThrow(() -> new IllegalArgumentException(
                                                "Question round not found: "
                                                                + question.getId()));
        }

        private List<QuizQuestion> getRoundQuestions(QuizRound round) {

                if (round.getQuestions() != null &&
                                !round.getQuestions().isEmpty()) {

                        return round.getQuestions();
                }

                if (round.getStages() != null &&
                                !round.getStages().isEmpty()) {

                        return round.getStages();
                }

                return List.of();
        }
}
