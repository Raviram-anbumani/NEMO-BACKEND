package com.nemo.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.ProgressResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.model.quiz.QuizQuestion;
import com.nemo.backend.model.quiz.QuizRound;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.QuestionProgressRepository;

@Service
public class ProgressService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionProgressRepository questionProgressRepository;
    private final QuizDataService quizDataService;

    public ProgressService(
            GameSessionRepository gameSessionRepository,
            QuestionProgressRepository questionProgressRepository,
            QuizDataService quizDataService
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionProgressRepository = questionProgressRepository;
        this.quizDataService = quizDataService;
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgress(String sessionId) {

        GameSession session =
                gameSessionRepository.findById(sessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Session not found"
                                )
                        );

        List<QuestionProgress> progressList =
                questionProgressRepository.findBySession(session);

        // Questions that have been solved correctly.
        List<String> completedQuestionIds =
                progressList.stream()
                        .filter(progress ->
                                Boolean.TRUE.equals(progress.getCorrect()))
                        .map(QuestionProgress::getQuestionId)
                        .toList();

        // Determine which rounds are completely solved.
        List<Integer> completedRounds = new ArrayList<>();

        for (QuizRound round : quizDataService.getRounds()) {

            List<QuizQuestion> roundQuestions;

            // Normal rounds use "questions".
            if (round.getQuestions() != null &&
                    !round.getQuestions().isEmpty()) {

                roundQuestions = round.getQuestions();

            } else {
                // Round 10 uses "stages".
                roundQuestions = round.getStages();
            }

            boolean roundCompleted =
                    roundQuestions != null &&
                    !roundQuestions.isEmpty() &&
                    roundQuestions.stream()
                            .allMatch(question ->
                                    completedQuestionIds.contains(
                                            question.getId()
                                    )
                            );

            if (roundCompleted) {
                completedRounds.add(round.getId());
            }
        }

        // Sequential unlocking.
        int unlockedRoundId = 1;

        for (int roundId = 1; roundId <= 10; roundId++) {

            if (completedRounds.contains(roundId)) {
                unlockedRoundId = roundId + 1;
            } else {
                break;
            }
        }

        // Do not return a round beyond the actual quiz.
        if (unlockedRoundId > 10) {
            unlockedRoundId = 10;
        }

        return new ProgressResponse(
                session.getId(),
                session.getPlayer().getName(),
                session.getPlayer().getEmail(),
                session.getTotalActiveTimeMs(),
                completedQuestionIds,
                completedRounds,
                unlockedRoundId
        );
    }
}