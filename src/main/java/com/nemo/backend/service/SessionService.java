package com.nemo.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.nemo.backend.dto.CreateSessionRequest;
import com.nemo.backend.dto.SessionResponse;
import com.nemo.backend.entity.GameSession;
import com.nemo.backend.entity.Player;
import com.nemo.backend.repository.GameSessionRepository;
import com.nemo.backend.repository.PlayerRepository;

@Service
public class SessionService {

    private final PlayerRepository playerRepository;
    private final GameSessionRepository gameSessionRepository;

    public SessionService(
            PlayerRepository playerRepository,
            GameSessionRepository gameSessionRepository
    ) {
        this.playerRepository = playerRepository;
        this.gameSessionRepository = gameSessionRepository;
    }

    @Transactional
    public SessionResponse createSession(CreateSessionRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("Request cannot be empty");
        }

        String name = request.getName() == null
                ? ""
                : request.getName().trim();

        String email = request.getEmail() == null
                ? ""
                : request.getEmail().trim().toLowerCase();

        // ============================================================
        // VALIDATE NAME
        // ============================================================

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException(
                    "Name must not exceed 100 characters"
            );
        }

        // ============================================================
        // VALIDATE EMAIL
        // ============================================================

        if (email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!email.matches("^[^\\s@]+@svce\\.ac\\.in$")) {
            throw new IllegalArgumentException(
                    "Only @svce.ac.in email addresses are eligible"
            );
        }

        // ============================================================
        // FIND OR CREATE PLAYER
        // ============================================================

        Player player = playerRepository
                .findByEmail(email)
                .orElseGet(() -> {

                    Player newPlayer = new Player();

                    newPlayer.setName(name);
                    newPlayer.setEmail(email);

                    return playerRepository.save(newPlayer);
                });

        // ============================================================
        // UPDATE NAME
        // ============================================================

        /*
         * Email is the permanent participant identity.
         *
         * The name can be updated if the participant enters
         * a different spelling when returning.
         */
        if (!name.equals(player.getName())) {
            player.setName(name);
            playerRepository.save(player);
        }

        // ============================================================
        // ONE PARTICIPANT = ONE PERMANENT SESSION
        // ============================================================

        /*
         * IMPORTANT:
         *
         * Do NOT create a new GameSession every time this endpoint
         * is called.
         *
         * If this email already has a session:
         *
         *     return that SAME session.
         *
         * If this email has never started:
         *
         *     create exactly ONE session.
         *
         * This allows:
         *
         *     Laptop -> Level 5 -> Exit
         *                  |
         *                  v
         *     Phone -> same email -> Level 6
         *
         * The session ID remains the same.
         */

        GameSession session = gameSessionRepository
                .findFirstByPlayerOrderByStartedAtAsc(player)
                .orElseGet(() -> {

                    GameSession newSession = new GameSession();

                    newSession.setPlayer(player);

                    return gameSessionRepository.save(newSession);
                });

        // ============================================================
        // RETURN PERMANENT SESSION
        // ============================================================

        return new SessionResponse(
                true,
                session.getId(),
                player.getName(),
                player.getEmail(),
                session.getStartedAt()
        );
    }
}