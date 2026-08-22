package com.yebin.sideproject.global.exception;

import com.yebin.sideproject.global.response.BaseResponse;
import com.yebin.sideproject.global.response.code.BaseResponseCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // BaseException을 상속한 도메인 예외는 각자 들고 있는 errorCode로 응답을 내려준다
    // (예외 타입별 핸들러를 추가할 필요가 없어, 핸들러 등록 누락으로 500이 발생하는 문제를 원천 차단한다)
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException e) {
        BaseResponseCode errorCode = e.getErrorCode();
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(BaseResponse.onFailure(errorCode, null)); // 에러응답에는 애초에 줄 응답이 없기 때문에 null로 줘야함
    }
}
