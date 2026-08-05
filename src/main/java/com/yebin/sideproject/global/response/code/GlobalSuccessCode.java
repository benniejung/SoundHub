package com.yebin.sideproject.global.response.code;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static com.yebin.sideproject.global.constant.StaticValue.CREATED;
import static com.yebin.sideproject.global.constant.StaticValue.OK;

@Getter
@AllArgsConstructor
public enum GlobalSuccessCode implements BaseResponseCode {
    SUCCESS_OK( "SUCCESS_200", OK,"호출에 성공하였습니다."),
    SUCCESS_CREATED("CREATED_201", CREATED, "생성에 성공하였습니다.");

    private final String code;
    private final int httpStatus;
    private final String message;

}
