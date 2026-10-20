package com.yebin.sideproject.global.response.code;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

public interface BaseResponseCode {
    HttpStatus getHttpStatus();
    String getCode();
    String getMessage();

    default ProblemDetail toProblemDetail(String instance) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(getHttpStatus(), getMessage());
        pd.setInstance(URI.create(instance));
        pd.setProperty("code", getCode());
        return pd;
    }
}
