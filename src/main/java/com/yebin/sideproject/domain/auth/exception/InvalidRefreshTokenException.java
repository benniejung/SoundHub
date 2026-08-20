package com.yebin.sideproject.domain.auth.exception;

import com.yebin.sideproject.global.exception.BaseException;
import com.yebin.sideproject.global.response.code.BaseResponseCode;

public class InvalidRefreshTokenException extends BaseException {

    public InvalidRefreshTokenException(BaseResponseCode errorCode) {
        super(errorCode);
    }
}
