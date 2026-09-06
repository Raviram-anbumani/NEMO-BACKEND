package com.nemo.backend.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.nemo.backend.entity.GameSession;

public interface GameSessionRepository extends JpaRepository<GameSession, String> {

    List<GameSession> findByStatusOrderByTotalActiveTimeMsAscCompletedAtAsc(
            GameSession.Status status,
            Pageable pageable
    );
}