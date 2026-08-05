package com.yebin.sideproject.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@RequiredArgsConstructor
public class BaseResponse<T> {
    private final Boolean isSuccess;
    private final String code; // 커스텀 응답 코드 (ex. A01, B02 등)
    private final String message; // 응답 메시지
    private final String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    // 성공 응답을 위한 정적 메서드
    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(true, "200", "요청 성공");
    }

    // 실패 응답을 위한 정적 메서드
    public static <T> BaseResponse<T> error(String code, String message) {
        return new BaseResponse<>(false, code, message);
    }
    
}
