package com.yebin.sideproject.global.exception;

import com.yebin.sideproject.domain.auth.exception.AuthException;
import com.yebin.sideproject.global.response.BaseResponse;
import com.yebin.sideproject.global.response.code.BaseResponseCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ProblemDetail handleBaseException(BaseException e, HttpServletRequest request) {
        BaseResponseCode errorCode = e.getErrorCode();


        log.warn("[Error] [{}] {}", errorCode.getCode(), errorCode.getMessage());
        return errorCode.toProblemDetail(request.getRequestURI());
    }

    // 로그인 인증 실패 (401)
    @ExceptionHandler(AuthException.class)
    public ProblemDetail handleServerError(BaseException e, HttpServletRequest request) {
        BaseResponseCode errorCode = e.getErrorCode();


        log.warn("[AUDIT] 인증 실패(401) - {} {} from {} : {}",
                request.getMethod(), request.getRequestURI(), clientIp(request), e.getMessage());
        return errorCode.toProblemDetail(request.getRequestURI());
    }





    /**
     * 클라이언트 IP 추출. 프록시 뒤라면 X-Forwarded-For 의 첫 IP 를 사용한다.
     * ( 감사 로그에 출처를 남기기 위한 용도 )
     */
    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
