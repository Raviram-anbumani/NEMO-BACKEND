package com.nemo.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.Player;

public interface GameSessionRepository extends JpaRepository<GameSession, String> {

    List<GameSession> findByStatusOrderByTotalActiveTimeMsAscCompletedAtAsc(
            GameSession.Status status,
            Pageable pageable
    );

    /*
     * One participant = one permanent NEMO session.
     *
     * Used by SessionService to find the participant's
     * existing session when they log in again.
     */
    Optional<GameSession> findFirstByPlayerOrderByStartedAtAsc(Player player);
}