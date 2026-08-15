package com.yebin.sideproject.domain.auth.exception;

import com.yebin.sideproject.global.exception.BaseException;
import com.yebin.sideproject.global.response.code.BaseResponseCode;
import com.yebin.sideproject.global.response.code.GlobalErrorCode;

public class DuplicateEmailException extends BaseException {
    public DuplicateEmailException(BaseResponseCode errorCode) {
        super(errorCode);
    }
}
