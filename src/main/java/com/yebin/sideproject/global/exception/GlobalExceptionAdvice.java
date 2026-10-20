package com.yebin.sideproject.global.exception;

import com.yebin.sideproject.domain.auth.exception.AuthException;
import com.yebin.sideproject.global.response.BaseResponse;
import com.yebin.sideproject.global.response.code.BaseResponseCode;
import com.yebin.sideproject.global.response.code.GlobalErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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

    // 처리되지 않은 모든 예외 (500)
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpectedException(Exception e, HttpServletRequest request) {
        return internalError(e, request);
    }

    /**
     * 500 공통 처리. 내부 정보(예외 메시지·스택트레이스)를 외부에 노출하지 않고(fail-closed),
     * 상관 ID(traceId)로 외부 응답과 내부 로그를 연결한다.
     */
    private ProblemDetail internalError(Exception e, HttpServletRequest request) {
        String traceId = newTraceId();
        log.error("[ERROR][{}] {} {} : {}", traceId, request.getMethod(), request.getRequestURI(), e.getMessage(), e);

        // 일반 Exception 에는 에러코드가 없으므로 공통 500 코드를 사용한다
        ProblemDetail problemDetail = GlobalErrorCode.SERVER_ERROR.toProblemDetail(request.getRequestURI());
        // 외부에는 내부 메시지를 숨기고 일반 메시지 + traceId 만 제공한다
        problemDetail.setProperty("traceId", traceId);
        return problemDetail;
    }

    /** 오류 상관 ID : 외부 응답과 내부 로그를 잇는 짧은 식별자 */
    private String newTraceId() {
        return java.util.UUID.randomUUID().toString().substring(0, 8);
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
