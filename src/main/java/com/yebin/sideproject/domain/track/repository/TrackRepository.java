package com.yebin.sideproject.domain.track.repository;

import com.yebin.sideproject.domain.track.entity.Track;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TrackRepository extends JpaRepository<Track, Long> {

    // 로그인한 크리에이터 본인이 업로드한 음원 목록 조회 (상태 무관, 최신순)
    List<Track> findByUserIdOrderByCreatedAtDesc(Long userId);

}
