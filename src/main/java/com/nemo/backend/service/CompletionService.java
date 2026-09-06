package com.nemo.backend.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.CompletionResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.QuestionProgressRepository;

@Service
public class CompletionService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionProgressRepository questionProgressRepository;

    public CompletionService(
            GameSessionRepository gameSessionRepository,
            QuestionProgressRepository questionProgressRepository
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionProgressRepository = questionProgressRepository;
    }

    @Transactional
    public CompletionResponse completeSession(String sessionId) {

        if (sessionId == null || sessionId.isBlank()) {
            throw new IllegalArgumentException("Session ID is required");
        }

        GameSession session =
                gameSessionRepository.findById(sessionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Session not found"
                                )
                        );

        if (session.getStatus() == GameSession.Status.COMPLETED) {
            throw new IllegalArgumentException(
                    "Session is already completed"
            );
        }

        List<QuestionProgress> progressList =
                questionProgressRepository.findBySession(session);

        Set<String> completedQuestionIds = new HashSet<>();

        for (QuestionProgress progress : progressList) {

            if (Boolean.TRUE.equals(progress.getCorrect())) {
                completedQuestionIds.add(progress.getQuestionId());
            }
        }

        // All 24 questions must be correctly completed.
        if (completedQuestionIds.size() < 24) {
            throw new IllegalArgumentException(
                    "Quiz is not completed. All 24 questions must be solved."
            );
        }

        // Round 10 must also be fully completed.
        if (session.getRound10Stage() == null ||
                session.getRound10Stage() != 5) {

            throw new IllegalArgumentException(
                    "Round 10 is not completed"
            );
        }

        LocalDateTime completedAt = LocalDateTime.now();

        session.setCompletedAt(completedAt);
        session.setStatus(GameSession.Status.COMPLETED);

        GameSession savedSession =
                gameSessionRepository.save(session);

        return new CompletionResponse(
                true,
                savedSession.getId(),
                savedSession.getPlayer().getName(),
                savedSession.getPlayer().getEmail(),
                savedSession.getTotalActiveTimeMs(),
                savedSession.getCompletedAt()
        );
    }
}