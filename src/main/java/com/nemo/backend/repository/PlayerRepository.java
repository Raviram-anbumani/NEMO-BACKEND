package com.nemo.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nemo.backend.entity.Player;

public interface PlayerRepository extends JpaRepository<Player, Long> {

    Optional<Player> findByEmail(String email);
}