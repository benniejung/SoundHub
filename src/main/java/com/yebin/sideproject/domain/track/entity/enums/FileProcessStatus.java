package com.yebin.sideproject.domain.track.entity.enums;

public enum FileProcessStatus {
    PENDING, // 업로드 대기
    VALIDATING, // 검증중 (파일크기/중복/손상 여부)
    PROCESSING, // 처리중 (트랜스코딩, 리사이징)
    READY, // 처리완료
    FAILED // 처리실패
}
