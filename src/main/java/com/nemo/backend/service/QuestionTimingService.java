package com.nemo.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.QuestionTimingResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.QuestionProgress;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.QuestionProgressRepository;

@Service
public class QuestionTimingService {

    private final GameSessionRepository gameSessionRepository;
    private final QuestionProgressRepository questionProgressRepository;
    private final QuizDataService quizDataService;

    public QuestionTimingService(
            GameSessionRepository gameSessionRepository,
            QuestionProgressRepository questionProgressRepository,
            QuizDataService quizDataService
    ) {
        this.gameSessionRepository = gameSessionRepository;
        this.questionProgressRepository = questionProgressRepository;
        this.quizDataService = quizDataService;
    }

    @Transactional
    public QuestionTimingResponse startQuestion(
            String sessionId,
            String questionId
    ) {

        GameSession session = getActiveSession(sessionId);

        // Make sure the question actually exists.
        quizDataService.getQuestion(questionId);

        QuestionProgress progress
                = questionProgressRepository
                        .findBySessionAndQuestionId(session, questionId)
                        .orElseGet(() -> {

                            QuestionProgress newProgress
                                    = new QuestionProgress();

                            newProgress.setSession(session);
                            newProgress.setQuestionId(questionId);

                            return newProgress;
                        });

        // Already active → don't restart the timer.
        if (progress.getActiveStartedAt() == null) {
            progress.setActiveStartedAt(LocalDateTime.now());
            questionProgressRepository.save(progress);
        }

        return buildResponse(session, progress);
    }

    @Transactional
    public QuestionTimingResponse stopQuestion(
            String sessionId,
            String questionId
    ) {

        GameSession session = getActiveSession(sessionId);

        quizDataService.getQuestion(questionId);

        QuestionProgress progress
                = questionProgressRepository
                        .findBySessionAndQuestionId(session, questionId)
                        .orElseGet(() -> {

                            QuestionProgress newProgress
                                    = new QuestionProgress();

                            newProgress.setSession(session);
                            newProgress.setQuestionId(questionId);

                            return newProgress;
                        });

        // Nothing is currently running.
        if (progress.getActiveStartedAt() == null) {
            return buildResponse(session, progress);
        }

        LocalDateTime now = LocalDateTime.now();

        long elapsedMs
                = Duration.between(
                        progress.getActiveStartedAt(),
                        now
                ).toMillis();

        // Protect against unexpected clock issues.
        if (elapsedMs < 0) {
            elapsedMs = 0;
        }

        progress.setTimeTakenMs(
                progress.getTimeTakenMs() + elapsedMs
        );

        progress.setActiveStartedAt(null);

        session.setTotalActiveTimeMs(
                session.getTotalActiveTimeMs() + elapsedMs
        );

        questionProgressRepository.save(progress);
        gameSessionRepository.save(session);

        return buildResponse(session, progress);
    }


    private GameSession getActiveSession(String sessionId) {

        GameSession session
                = gameSessionRepository.findById(sessionId)
                        .orElseThrow(()
                                -> new IllegalArgumentException(
                                "Session not found"
                        )
                        );

        if (session.getStatus() != GameSession.Status.ACTIVE) {
            throw new IllegalArgumentException(
                    "Session is not active"
            );
        }

        return session;
    }

    private QuestionTimingResponse buildResponse(
            GameSession session,
            QuestionProgress progress
    ) {

        long activeMs = progress.getTimeTakenMs();

        if (progress.getActiveStartedAt() != null) {

            long currentInterval
                    = Duration.between(
                            progress.getActiveStartedAt(),
                            LocalDateTime.now()
                    ).toMillis();

            if (currentInterval > 0) {
                activeMs += currentInterval;
            }
        }

        return new QuestionTimingResponse(
                true,
                session.getId(),
                progress.getQuestionId(),
                activeMs,
                session.getTotalActiveTimeMs(),
                progress.getActiveStartedAt() != null
        );
    }
}
