package com.yebin.sideproject.global.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.yebin.sideproject.global.response.code.BaseResponseCode;
import com.yebin.sideproject.global.response.code.GlobalErrorCode;
import com.yebin.sideproject.global.response.code.GlobalSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
@JsonPropertyOrder({"isSuccess", "code", "message", "result", "timeStamp"})
public class BaseResponse<T> {
    private final Boolean isSuccess;
    private final String code; // 커스텀 응답 코드 (ex. A01, B02 등)
    private final String message; // 응답 메시지
    private final T result; // 데이터 응답 결과
    private final String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

    // 성공 응답을 위한 정적 메서드
    public static <T> BaseResponse<T> onSuccess(BaseResponseCode code, T result) {
        return new BaseResponse<>(true, code.getCode(), code.getMessage(), result);
    }

    // 실패 응답을 위한 정적 메서드
    public static <T> BaseResponse<T> onFailure(BaseResponseCode code, T result) {
        return new BaseResponse<>(false, code.getCode(), code.getMessage(), result);
    }
}
