package com.yebin.sideproject.global.exception;

import com.yebin.sideproject.global.response.code.BaseResponseCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException{
    private final BaseResponseCode errorCode;
}
