package com.yebin.sideproject.domain.track.repository;

import com.yebin.sideproject.domain.chatbot.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByEmbeddedFalse();
}
