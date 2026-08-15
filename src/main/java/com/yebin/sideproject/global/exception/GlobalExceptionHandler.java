package com.yebin.sideproject.global.exception;

import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.domain.auth.exception.DuplicateEmailException;
import com.yebin.sideproject.domain.auth.exception.InvalidRefreshTokenException;
import com.yebin.sideproject.global.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<BaseResponse<Void>> handleDuplicateEmail(DuplicateEmailException e) {
        AuthErrorCode errorCode = AuthErrorCode.DUPLICATE_EMAIL_ERROR;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.onFailure(errorCode, null)); // 에러응답에는 애초에 줄 응답이 없기 때문에 null로 줘야함
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        AuthErrorCode errorCode = AuthErrorCode.REFRESH_TOKEN_INVALID;
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.onFailure(errorCode, null));
    }
}
