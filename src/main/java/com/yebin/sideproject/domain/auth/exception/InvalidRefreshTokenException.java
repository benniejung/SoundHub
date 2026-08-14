package com.yebin.sideproject.domain.auth.exception;

public class InvalidRefreshTokenException extends RuntimeException {

    public InvalidRefreshTokenException() {
        super("유효하지 않거나 만료된 refresh token입니다.");
    }
}
