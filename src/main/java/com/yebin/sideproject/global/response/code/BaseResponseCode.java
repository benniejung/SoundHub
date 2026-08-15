package com.yebin.sideproject.global.response.code;

import org.springframework.http.HttpStatus;

public interface BaseResponseCode {
    HttpStatus getStatus();
    String getCode();
    String getMessage();
}
