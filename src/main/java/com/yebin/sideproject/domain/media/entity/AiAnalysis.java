package com.yebin.sideproject.domain.media.entity;

import com.yebin.sideproject.domain.chatbot.entity.Song;
import com.yebin.sideproject.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "ai_analysis")
@Getter
@NoArgsConstructor
public class AiAnalysis extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", nullable = false)
    private Song track;

    @Column(name = "inappropriate_flag", nullable = false)
    private boolean inappropriateFlag;

    @Column(name = "lyrics_text", columnDefinition = "TEXT")
    private String lyricsText;

    @Builder
    public AiAnalysis(Song track, boolean inappropriateFlag, String lyricsText) {
        this.track = track;
        this.inappropriateFlag = inappropriateFlag;
        this.lyricsText = lyricsText;
    }
}
