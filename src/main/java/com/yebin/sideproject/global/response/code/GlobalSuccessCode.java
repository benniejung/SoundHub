package com.yebin.sideproject.global.response.code;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static com.yebin.sideproject.global.constant.StaticValue.CREATED;
import static com.yebin.sideproject.global.constant.StaticValue.OK;

@Getter
@RequiredArgsConstructor
public enum GlobalSuccessCode implements BaseResponseCode {
    SUCCESS_OK( HttpStatus.OK, "OK_200","호출에 성공하였습니다."),
    SUCCESS_CREATED(HttpStatus.CREATED, "CREATED_201", "생성에 성공하였습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
