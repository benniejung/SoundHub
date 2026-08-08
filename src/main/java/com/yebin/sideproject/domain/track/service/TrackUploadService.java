package com.yebin.sideproject.domain.track.service;

import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.entity.enums.Role;
import com.yebin.sideproject.domain.track.dto.TrackUploadRequestDto;
import com.yebin.sideproject.domain.track.dto.TrackUploadResponseDto;
import com.yebin.sideproject.domain.track.entity.Track;
import com.yebin.sideproject.domain.track.entity.TrackFile;
import com.yebin.sideproject.domain.track.entity.enums.FileCategory;
import com.yebin.sideproject.domain.track.entity.enums.FileType;
import com.yebin.sideproject.domain.track.entity.enums.TrackStatus;
import com.yebin.sideproject.domain.track.repository.TrackFileRepository;
import com.yebin.sideproject.domain.track.repository.TrackRepository;
import com.yebin.sideproject.global.exception.BaseException;
import com.yebin.sideproject.global.response.code.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional
public class TrackUploadService {

    private static final Duration PRESIGN_EXPIRATION = Duration.ofMinutes(10); // 보통 5분~15분 사이이므로 그 중간값인 10분으로 정함

    private final TrackRepository trackRepository;
    private final TrackFileRepository trackFileRepository;
    private final S3Presigner s3Presigner;

    @Value("${minio.soundhub-audio}")
    private String audioBucket;

    @Value("${minio.soundhub-thumbnail}")
    private String thumbnailBucket;

    public TrackUploadResponseDto requestUpload(User creator, TrackUploadRequestDto request) {
        // 1. 크리에이터 권한 검증 -> 크리에이터가 아니면 에러 반환
        if (creator.getRole() != Role.CREATOR) {
            throw new BaseException(GlobalErrorCode.ACCESS_DENIED_REQUEST);
        }

        // 2. 음원 DB에 저장 -> 음원 상태 업로드로 변경 -> trackId 확보
        Track track = trackRepository.save(Track.builder()
                .user(creator)
                .title(request.title())
                .genre(request.genre())
                .description(request.description())
                .status(TrackStatus.UPLOADED)
                .build());

        // 3. 확보한 trackId로 경로 생성 -> DB에 경로 저장
        String audioKey = "tracks/%d/audio/%s".formatted(track.getId(), request.audioFileName());
        String thumbnailKey = "tracks/%d/thumbnail/%s".formatted(track.getId(), request.thumbnailFileName());

        trackFileRepository.save(TrackFile.builder()
                .track(track)
                .fileCategory(FileCategory.AUDIO)
                .fileType(FileType.ORIGINAL)
                .storagePath(audioKey)
                .build());

        trackFileRepository.save(TrackFile.builder()
                .track(track)
                .fileCategory(FileCategory.IMAGE)
                .fileType(FileType.THUMBNAIL_ORIGINAL)
                .storagePath(thumbnailKey)
                .build());

        // 4. Docker Minio와 파일 버킷 연결 (presigned URL 발급)
        String audioUploadUrl = presignPutUrl(audioBucket, audioKey, request.audioContentType());
        String thumbnailUploadUrl = presignPutUrl(thumbnailBucket, thumbnailKey, request.thumbnailContentType());

        return new TrackUploadResponseDto(
                String.valueOf(track.getId()),
                audioUploadUrl,
                thumbnailUploadUrl,
                (int) PRESIGN_EXPIRATION.toSeconds()
        );
    }

    private String presignPutUrl(String bucket, String key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(PutObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_EXPIRATION)
                .putObjectRequest(putObjectRequest)
                .build());

        return presigned.url().toString();
    }
}
