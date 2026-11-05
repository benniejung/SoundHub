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
    DUPLICATE_EMAIL_ERROR(CONFLICT, "AUTH_409_1", "이미 가입된 이메일입니다."),
    DUPLICATE_NICKNAME_ERROR(CONFLICT, "AUTH_409_2", "이미 가입된 닉네임니다."),
    SMS_RESEND_TOO_SOON(TOO_MANY_REQUESTS, "AUTH_429_1", "10초 후에 다시 시도해 주세요."),
    SMS_SEND_LIMIT_EXCEEDED(TOO_MANY_REQUESTS, "AUTH_429_2", "인증번호 요청 횟수를 초과했습니다. 1시간 후 다시 시도해 주세요."),
    SMS_SEND_FAILED(BAD_GATEWAY, "AUTH_502_1", "인증번호 발송에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
