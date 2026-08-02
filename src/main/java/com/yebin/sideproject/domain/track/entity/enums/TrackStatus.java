package com.yebin.sideproject.domain.track.entity.enums;

public enum TrackStatus {
    UPLOADED, // 전송완료
    VALIDATING, // 검증중
    PROCESSING, // 처리중
    REVIEW_PENDING, // 검수대기
    PUBLISHED, // 공개
    REJECTED // 반려
}
