package com.yebin.sideproject.global.exception;

import com.yebin.sideproject.global.response.code.BaseResponseCode;
import com.yebin.sideproject.global.response.code.GlobalErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public class BaseException extends RuntimeException{
     private final BaseResponseCode errorCode;

}
