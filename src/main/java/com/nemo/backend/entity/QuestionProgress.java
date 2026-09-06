package com.nemo.backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "question_progress",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_session_question",
            columnNames = {"session_id", "question_id"}
        )
    }
)
public class QuestionProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private GameSession session;

    @Column(name = "question_id", nullable = false, length = 20)
    private String questionId;

    @Column(name = "attempt_count", nullable = false)
    private Integer attemptCount = 0;

    @Column(nullable = false)
    private Boolean correct = false;

    @Column(name = "time_taken_ms", nullable = false)
    private Long timeTakenMs = 0L;

    @Column(name = "active_started_at")
    private LocalDateTime activeStartedAt;

    public Long getId() {
        return id;
    }

    public GameSession getSession() {
        return session;
    }

    public void setSession(GameSession session) {
        this.session = session;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public Long getTimeTakenMs() {
        return timeTakenMs;
    }

    public void setTimeTakenMs(Long timeTakenMs) {
        this.timeTakenMs = timeTakenMs;
    }

    public LocalDateTime getActiveStartedAt() {
        return activeStartedAt;
    }

    public void setActiveStartedAt(LocalDateTime activeStartedAt) {
        this.activeStartedAt = activeStartedAt;
    }
}