package com.yebin.sideproject.domain.track.repository;

import com.yebin.sideproject.domain.track.entity.TrackFile;
import com.yebin.sideproject.domain.track.entity.enums.FileCategory;
import com.yebin.sideproject.domain.track.entity.enums.FileProcessStatus;
import com.yebin.sideproject.domain.track.entity.enums.FileType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TrackFileRepository extends JpaRepository<TrackFile, Long> {

    // 파일 중복 확인하기 위한 메서드 (해시는 파일의 고유값을 저장한다)
    boolean existsByFileHash(String fileHash);

    // S3에 등록된 파일을 찾기 위한 메서드
    Optional<TrackFile> findByStoragePath(String storagePath);

    // 여러 트랙의 썸네일 파일을 한 번에 조회하기 위한 메서드 (목록 조회 시 N+1 방지)
    List<TrackFile> findByTrackIdInAndFileCategoryAndFileType(
            List<Long> trackIds, FileCategory fileCategory, FileType fileType);

    // 처리 상태 변경 메서드
    @Modifying
    @Query("UPDATE TrackFile tf SET tf.fileProcessStatus = :status, tf.updatedAt = CURRENT_TIMESTAMP WHERE tf.id = :id")
    int updateFileProcessStatus(@Param("id") Long id, @Param("status") FileProcessStatus status);

    // 포맷을 저장하는 메서드 (용량은 저장하지 않음, bitrate는 추후 트랜스코딩 단계에서 별도로 저장)
    @Modifying
    @Query("UPDATE TrackFile tf SET tf.format = :format, tf.updatedAt = CURRENT_TIMESTAMP WHERE tf.id = :id")
    int updateFormat(@Param("id") Long id, @Param("format") String format);
}
