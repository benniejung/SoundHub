package com.yebin.sideproject.domain.media.exception;

public class FileProcessingException extends RuntimeException {

    public FileProcessingException(Throwable cause) {
        super("파일을 처리하는 중 오류가 발생했습니다.", cause);
    }
}
