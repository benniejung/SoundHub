package com.yebin.sideproject.domain.media.entity.enums;

public enum PipelineStage {
    VALIDATION, // 검증
    TRANSCODING, // 트랜스코딩
    AI_ANALYSIS, // AI분석
    REVIEW, // 검수
    DISTRIBUTION // 배포
}
