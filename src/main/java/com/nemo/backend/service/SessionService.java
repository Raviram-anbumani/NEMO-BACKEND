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

        // Validate name
        if (name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (name.length() > 100) {
            throw new IllegalArgumentException("Name must not exceed 100 characters");
        }

        // Validate email
        if (email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (!email.matches("^[^\\s@]+@svce\\.ac\\.in$")) {
            throw new IllegalArgumentException(
                    "Only @svce.ac.in email addresses are eligible"
            );
        }

        // Find existing player or create new player
        Player player = playerRepository
                .findByEmail(email)
                .orElseGet(() -> {
                    Player newPlayer = new Player();
                    newPlayer.setName(name);
                    newPlayer.setEmail(email);
                    return playerRepository.save(newPlayer);
                });

        // Keep the latest submitted name
        if (!player.getName().equals(name)) {
            player.setName(name);
            playerRepository.save(player);
        }

        // Create new game session
        GameSession session = new GameSession();
        session.setPlayer(player);

        GameSession savedSession =
                gameSessionRepository.save(session);

        return new SessionResponse(
                true,
                savedSession.getId(),
                player.getName(),
                player.getEmail(),
                savedSession.getStartedAt()
        );
    }
}