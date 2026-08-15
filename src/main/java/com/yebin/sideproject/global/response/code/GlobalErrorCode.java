package com.yebin.sideproject.global.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@AllArgsConstructor
public enum GlobalErrorCode implements BaseResponseCode {

    BAD_REQUEST_ERROR(BAD_REQUEST, "GLOBAL_400", "잘못된 요청입니다."),
    UNAUTHENTICATED_REQUEST(UNAUTHORIZED, "GLOBAL_401", "인증되지 않았습니다."),
    ACCESS_DENIED_REQUEST(FORBIDDEN, "GLOBAL_403", "해당 요청에 접근 권한이 없습니다."),
    ENDPOINT_NOT_FOUND(NOT_FOUND, "GLOBAL_404", "해당 엔드포인트를 찾을 수 없습니다."),
    UNSUPPORTED_HTTP_METHOD(METHOD_NOT_ALLOWED, "GLOBAL_405", "지원하지 않는 HTTP 메서드입니다."),
    SERVER_ERROR(INTERNAL_SERVER_ERROR, "GLOBAL_500", "서버 내부에서 알 수 없는 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
