package com.nemo.backend.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.AttemptResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.QuestionProgressRepository;

@Service
public class AttemptService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionProgressRepository questionProgressRepository;

    public AttemptService(
            GameSessionRepository gameSessionRepository,
            QuestionProgressRepository questionProgressRepository
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionProgressRepository = questionProgressRepository;
    }

    @Transactional(readOnly = true)
    public List<AttemptResponse> getAttempts(String sessionId) {

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

        List<QuestionProgress> progressList =
                questionProgressRepository.findBySession(session);

        return progressList.stream()
                .map(progress ->
                        new AttemptResponse(
                                progress.getQuestionId(),
                                progress.getAttemptCount(),
                                Boolean.TRUE.equals(
                                        progress.getCorrect()
                                ),
                                progress.getTimeTakenMs()
                        )
                )
                .toList();
    }
}