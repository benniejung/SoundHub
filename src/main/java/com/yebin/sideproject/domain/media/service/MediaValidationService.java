package com.yebin.sideproject.domain.media.service;

import com.yebin.sideproject.domain.chatbot.entity.Song;
import com.yebin.sideproject.domain.media.entity.Pipeline;
import com.yebin.sideproject.domain.media.entity.enums.PipelineStage;
import com.yebin.sideproject.domain.media.entity.enums.PipelineStatus;
import com.yebin.sideproject.domain.media.exception.FileSizeExceededException;
import com.yebin.sideproject.domain.media.repository.MediaValidationRepository;
import com.yebin.sideproject.domain.media.repository.PipelineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class MediaValidationService {

    private static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB

    private final MediaValidationRepository mediaValidationRepository;
    private final PipelineRepository pipelineRepository;

    // 1. 파일 크기 검증
    public void validateFileSize(Song track, MultipartFile file) {

    }

    // 2. 중복파일인지 검증
    public void validateDuplicateFile(MultipartFile file) {
    }

    // 3. 손상된 파일인지 검증
    public void validateCorruptedFile(MultipartFile file) {
    }
}
