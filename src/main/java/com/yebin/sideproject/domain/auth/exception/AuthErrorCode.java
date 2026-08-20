package com.yebin.sideproject.domain.auth.exception;

import com.yebin.sideproject.global.response.code.BaseResponseCode;
import com.yebin.sideproject.global.response.code.GlobalErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements BaseResponseCode {

    REFRESH_TOKEN_INVALID(UNAUTHORIZED, "AUTH_401_1", "리프레시 토큰이 유효하지 않습니다. 다시 로그인해주세요."),
    LOGIN_FAILED(UNAUTHORIZED, "AUTH_401_2", "이메일 또는 비밀번호가 올바르지 않습니다."),
    DUPLICATE_EMAIL_ERROR(CONFLICT, "AUTH_409_1", "이미 가입된 이메일입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
