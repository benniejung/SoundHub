package com.yebin.sideproject.domain.chatbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
public class Song {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String artist;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String cheerVersion;
    @Column(columnDefinition = "TEXT")
    private String lyrics;
    private boolean embedded;

    @Builder
    public Song(String title, String artist, String cheerVersion, String lyrics) {
        this.title = title;
        this.artist = artist;
        this.cheerVersion = cheerVersion;
        this.lyrics = lyrics;
        this.embedded = false;
    }

    public void markEmbedded() {
        this.embedded = true;
    }
}
