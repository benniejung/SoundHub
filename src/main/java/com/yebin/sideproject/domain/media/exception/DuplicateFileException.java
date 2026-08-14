package com.yebin.sideproject.domain.media.exception;

public class DuplicateFileException extends RuntimeException {

    public DuplicateFileException(String fileHash) {
        super("이미 존재하는 파일입니다. (hash=%s)".formatted(fileHash));
    }
}
