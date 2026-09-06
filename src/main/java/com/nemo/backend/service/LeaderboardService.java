package com.nemo.backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.LeaderboardResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.repository.GameSessionRepository;

@Service
public class LeaderboardService {

    private final GameSessionRepository gameSessionRepository;

    public LeaderboardService(
            GameSessionRepository gameSessionRepository
    ) {
        this.gameSessionRepository = gameSessionRepository;
    }

    @Transactional(readOnly = true)
    public List<LeaderboardResponse> getLeaderboard(int limit) {

        if (limit < 1) {
            throw new IllegalArgumentException(
                    "Limit must be at least 1"
            );
        }

        if (limit > 100) {
            limit = 100;
        }

        Pageable pageable = PageRequest.of(0, limit);

        List<GameSession> sessions =
                gameSessionRepository
                        .findByStatusOrderByTotalActiveTimeMsAscCompletedAtAsc(
                                GameSession.Status.COMPLETED,
                                pageable
                        );

        List<LeaderboardResponse> leaderboard =
                new ArrayList<>();

        int rank = 1;

        for (GameSession session : sessions) {

            leaderboard.add(
                    new LeaderboardResponse(
                            rank,
                            session.getPlayer().getName(),
                            session.getTotalActiveTimeMs(),
                            session.getCompletedAt()
                    )
            );

            rank++;
        }

        return leaderboard;
    }
}