package com.nemo.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nemo.backend.dto.LeaderboardResponse;
import com.nemo.backend.service.LeaderboardService;

@RestController
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    public LeaderboardController(
            LeaderboardService leaderboardService
    ) {
        this.leaderboardService = leaderboardService;
    }

    @GetMapping("/api/leaderboard")
    public ResponseEntity<List<LeaderboardResponse>> getLeaderboard(
            @RequestParam(defaultValue = "50") int limit
    ) {

        return ResponseEntity.ok(
                leaderboardService.getLeaderboard(limit)
        );
    }
}