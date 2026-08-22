package com.yebin.sideproject.domain.auth.exception;

import com.yebin.sideproject.global.exception.BaseException;
import com.yebin.sideproject.global.response.code.BaseResponseCode;

public class DuplicateNicknameException extends BaseException {

    public DuplicateNicknameException(BaseResponseCode errorCode) {
        super(errorCode);
    }
}
