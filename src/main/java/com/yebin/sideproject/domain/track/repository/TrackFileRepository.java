package com.yebin.sideproject.domain.track.repository;

import com.yebin.sideproject.domain.track.entity.TrackFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackFileRepository extends JpaRepository<TrackFile, Long> {

    boolean existsByFileHash(String fileHash);
}
