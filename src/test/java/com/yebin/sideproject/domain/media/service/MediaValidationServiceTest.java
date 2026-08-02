package com.yebin.sideproject.domain.media.service;

import com.yebin.sideproject.domain.chatbot.entity.Song;
import com.yebin.sideproject.domain.media.entity.Pipeline;
import com.yebin.sideproject.domain.media.exception.FileSizeExceededException;
import com.yebin.sideproject.domain.media.repository.MediaValidationRepository;
import com.yebin.sideproject.domain.media.repository.PipelineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("파일 검증 테스트")
class MediaValidationServiceTest {

    @Mock
    private MediaValidationRepository mediaValidationRepository;

    @Mock
    private PipelineRepository pipelineRepository;

    @InjectMocks
    private MediaValidationService mediaValidationService;

    private Song track;
    private MultipartFile file;

    @BeforeEach
    void setUp() {
        long fileSize = 150 * 1024 * 1024; // 100MB 제한을 초과하는 150MB 파일

        track = mock(Song.class);
        file = mock(MultipartFile.class);
        when(file.getSize()).thenReturn(fileSize);
    }

    @Test
    @DisplayName("파일크기 검증 테스트")
    void validateFileSizeTest() {
        // 파일이 크면 파이프라인 DB에 상태 저장 + 검증 중단
        assertThrows(FileSizeExceededException.class,
                () -> mediaValidationService.validateFileSize(track, file));

        verify(pipelineRepository).save(any(Pipeline.class));
    }

    @Test
    @DisplayName("중복파일 검증 테스트")
    void validateDuplicateFileTest() {

    }

    @Test
    @DisplayName("손상파일 검증 테스트")
    void validateCorruptedFile() {

    }
}
