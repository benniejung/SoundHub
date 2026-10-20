package com.yebin.sideproject.domain.auth.exception;

import com.yebin.sideproject.global.exception.BaseException;
import com.yebin.sideproject.global.response.code.BaseResponseCode;
import com.yebin.sideproject.global.response.code.GlobalErrorCode;

public class DuplicateNicknameException extends BaseException {

    public DuplicateNicknameException(BaseResponseCode errorCode) {
        super((GlobalErrorCode) errorCode);
    }
}
