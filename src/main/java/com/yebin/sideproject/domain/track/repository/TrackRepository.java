package com.yebin.sideproject.domain.track.repository;

import com.yebin.sideproject.domain.track.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackRepository extends JpaRepository<Track, Long> {
}
