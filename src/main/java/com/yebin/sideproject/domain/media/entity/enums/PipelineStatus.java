package com.yebin.sideproject.domain.media.entity.enums;

public enum PipelineStatus {
    WAITING, // 대기
    PROCESSING, // 처리중
    COMPLETED, // 완료
    FAILED, // 실패
    RETRYING // 재처리중
}
