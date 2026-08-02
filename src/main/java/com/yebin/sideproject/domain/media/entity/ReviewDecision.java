package com.yebin.sideproject.domain.media.entity;

import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.chatbot.entity.Song;
import com.yebin.sideproject.domain.media.entity.enums.DecisionType;
import com.yebin.sideproject.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_decisions")
@Getter
@NoArgsConstructor
public class ReviewDecision extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Song track;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DecisionType decision;

    @Column(length = 500)
    private String reason;

    @Builder
    public ReviewDecision(Song track, User user, DecisionType decision, String reason) {
        this.track = track;
        this.user = user;
        this.decision = decision;
        this.reason = reason;
    }
}
