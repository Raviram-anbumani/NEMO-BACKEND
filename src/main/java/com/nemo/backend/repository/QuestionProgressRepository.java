package com.nemo.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;

public interface QuestionProgressRepository extends JpaRepository<QuestionProgress, Long> {

    Optional<QuestionProgress> findBySessionAndQuestionId(
            GameSession session,
            String questionId
    );

    List<QuestionProgress> findBySession(GameSession session);
}