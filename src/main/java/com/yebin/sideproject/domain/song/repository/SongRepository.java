package com.yebin.sideproject.domain.song.repository;

import com.yebin.sideproject.domain.entity.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {

    List<Song> findByEmbeddedFalse();
}
