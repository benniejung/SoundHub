package com.yebin.sideproject.domain.media.entity;

import com.yebin.sideproject.domain.chatbot.entity.Song;
import com.yebin.sideproject.domain.media.entity.enums.PipelineStage;
import com.yebin.sideproject.domain.media.entity.enums.PipelineStatus;
import com.yebin.sideproject.global.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
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
@Table(name = "pipeline")
@AttributeOverride(name = "createdAt", column = @Column(name = "started_at", updatable = false))
@Getter
@NoArgsConstructor
public class Pipeline extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Song track;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PipelineStage stage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PipelineStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "error_code", length = 50)
    private String errorCode;

    @Builder
    public Pipeline(Song track, PipelineStage stage, PipelineStatus status, int retryCount, String errorCode) {
        this.track = track;
        this.stage = stage;
        this.status = status;
        this.retryCount = retryCount;
        this.errorCode = errorCode;
    }
}
