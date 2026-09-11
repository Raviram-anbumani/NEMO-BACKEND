package com.nemo.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.LeaderboardResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.repository.GameSessionRepository;

@Service
public class LeaderboardService {

    private final GameSessionRepository gameSessionRepository;
    private final QuizProgressionService quizProgressionService;

    public LeaderboardService(
            GameSessionRepository gameSessionRepository,
            QuizProgressionService quizProgressionService
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.quizProgressionService = quizProgressionService;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getLeaderboard(int limit) {

        if (limit < 1) {
            throw new IllegalArgumentException("Limit must be at least 1");
        }

        if (limit > 100) {
            limit = 100;
        }

        List<GameSession> sessions = gameSessionRepository.findAll();

        List<LeaderboardEntry> entries = new ArrayList<>();

        for (GameSession session : sessions) {

            int completedRounds = 0;

            for (int roundId = 1; roundId <= 10; roundId++) {

                if (quizProgressionService.isRoundCompleted(session, roundId)) {
                    completedRounds++;
                }
            }

            boolean fullyCompleted = completedRounds == 10;

            entries.add(
                    new LeaderboardEntry(
                            session,
                            completedRounds,
                            fullyCompleted
                    )
            );
        }

        /*
         * Ranking rules:
         *
         * 1. Fully completed players come first.
         *
         * 2. Fully completed players:
         *      Lower total active time = better rank.
         *
         * 3. Incomplete players:
         *      More completed rounds = better rank.
         *
         * 4. If completed rounds are equal:
         *      Lower total active time = better rank.
         */

        entries.sort((a, b) -> {

            // Fully completed players always come first
            if (a.fullyCompleted != b.fullyCompleted) {
                return a.fullyCompleted ? -1 : 1;
            }

            // Both fully completed -> fastest time first
            if (a.fullyCompleted) {

                int timeCompare = Long.compare(
                        a.session.getTotalActiveTimeMs(),
                        b.session.getTotalActiveTimeMs()
                );

                if (timeCompare != 0) {
                    return timeCompare;
                }
            }

            // Both incomplete -> more rounds completed first
            int roundsCompare = Integer.compare(
                    b.completedRounds,
                    a.completedRounds
            );

            if (roundsCompare != 0) {
                return roundsCompare;
            }

            // Same number of rounds -> fastest time first
            int timeCompare = Long.compare(
                    a.session.getTotalActiveTimeMs(),
                    b.session.getTotalActiveTimeMs()
            );

            if (timeCompare != 0) {
                return timeCompare;
            }

            // Final deterministic tie-breaker
            return a.session.getPlayer()
                    .getName()
                    .compareToIgnoreCase(
                            b.session.getPlayer().getName()
                    );
        });

        List<LeaderboardResponse> leaderboard = new ArrayList<>();

        int rank = 1;

        for (LeaderboardEntry entry : entries) {

            if (leaderboard.size() >= limit) {
                break;
            }

            GameSession session = entry.session;

            leaderboard.add(
                    new LeaderboardResponse(
                            rank,
                            session.getPlayer().getName(),

                            // Participant's unique identifier
                            session.getPlayer().getEmail(),

                            entry.completedRounds,
                            10,
                            entry.fullyCompleted,
                            session.getTotalActiveTimeMs(),
                            session.getCompletedAt()
                    )
            );

            rank++;
        }

        return leaderboard;
    }

    private static class LeaderboardEntry {

        private final GameSession session;
        private final int completedRounds;
        private final boolean fullyCompleted;

        private LeaderboardEntry(
                GameSession session,
                int completedRounds,
                boolean fullyCompleted
        ) {
            this.session = session;
            this.completedRounds = completedRounds;
            this.fullyCompleted = fullyCompleted;
        }
    }
}