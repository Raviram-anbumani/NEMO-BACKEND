package com.nemo.backend.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        name = "game_sessions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_game_session_player",
                        columnNames = "player_id"
                )
        }
)
public class GameSession {

    @Id
    @Column(length = 36)
    private String id;

    /*
     * One player can have only ONE game session.
     *
     * This is the database-level protection for:
     * "One participant/email = one permanent NEMO session."
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false, unique = true)
    private Player player;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;

    @Column(name = "total_active_time_ms", nullable = false)
    private Long totalActiveTimeMs = 0L;

    @Column(name = "round10_stage")
    private Integer round10Stage = 0;

    @Column(name = "round10_n")
    private Integer round10N;

    @Column(name = "round10_v")
    private Integer round10V;

    @Column(name = "round10_k")
    private Integer round10K;

    @Column(name = "round10_l", length = 1)
    private String round10L;

    @OneToMany(
            mappedBy = "session",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<QuestionProgress> questionProgress = new ArrayList<>();

    public enum Status {
        ACTIVE,
        COMPLETED
    }

    @PrePersist
    protected void onCreate() {

        if (id == null) {
            id = UUID.randomUUID().toString();
        }

        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }

        if (status == null) {
            status = Status.ACTIVE;
        }

        if (totalActiveTimeMs == null) {
            totalActiveTimeMs = 0L;
        }

        if (round10Stage == null) {
            round10Stage = 0;
        }
    }

    public String getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getTotalActiveTimeMs() {
        return totalActiveTimeMs;
    }

    public void setTotalActiveTimeMs(Long totalActiveTimeMs) {
        this.totalActiveTimeMs = totalActiveTimeMs;
    }

    public List<QuestionProgress> getQuestionProgress() {
        return questionProgress;
    }

    public Integer getRound10Stage() {
        return round10Stage;
    }

    public void setRound10Stage(Integer round10Stage) {
        this.round10Stage = round10Stage;
    }

    public Integer getRound10N() {
        return round10N;
    }

    public void setRound10N(Integer round10N) {
        this.round10N = round10N;
    }

    public Integer getRound10V() {
        return round10V;
    }

    public void setRound10V(Integer round10V) {
        this.round10V = round10V;
    }

    public Integer getRound10K() {
        return round10K;
    }

    public void setRound10K(Integer round10K) {
        this.round10K = round10K;
    }

    public String getRound10L() {
        return round10L;
    }

    public void setRound10L(String round10L) {
        this.round10L = round10L;
    }
}