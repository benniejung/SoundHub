package com.yebin.sideproject.domain.media.exception;

public class FileSizeExceededException extends RuntimeException {

    public FileSizeExceededException(long fileSize, long maxSize) {
        super("파일 크기가 허용된 최대 크기를 초과했습니다. (fileSize=%d, maxSize=%d)".formatted(fileSize, maxSize));
    }
}
