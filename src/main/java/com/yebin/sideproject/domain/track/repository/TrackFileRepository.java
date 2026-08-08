package com.yebin.sideproject.domain.track.repository;

import com.yebin.sideproject.domain.track.entity.TrackFile;
import com.yebin.sideproject.domain.track.entity.enums.FileProcessStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrackFileRepository extends JpaRepository<TrackFile, Long> {

    // 파일 중복 확인하기 위한 메서드 (해시는 파일의 고유값을 저장한다)
    boolean existsByFileHash(String fileHash);

    // S3에 등록된 파일을 찾기 위한 메서드
    Optional<TrackFile> findByStoragePath(String storagePath);

    // 처리 상태 변경 메서드
    @Modifying
    @Query("UPDATE TrackFile tf SET tf.fileProcessStatus = :status, tf.updatedAt = CURRENT_TIMESTAMP WHERE tf.id = :id")
    int updateFileProcessStatus(@Param("id") Long id, @Param("status") FileProcessStatus status);

    // 포맷과 용량을 저장하는 메서드
    @Modifying
    @Query("UPDATE TrackFile tf SET tf.format = :format, tf.bitrate = :bitrate, tf.updatedAt = CURRENT_TIMESTAMP WHERE tf.id = :id")
    int updateFormatAndFileSize(@Param("id") Long id, @Param("format") String format, @Param("bitrate") Long bitrate);
}
