package com.yebin.sideproject.domain.auth.exception;

public class DuplicateNicknameException extends RuntimeException{

    public DuplicateNicknameException(String nickname) {
        super("등록된 닉네임입니다. 다른 닉네임을 설정해주세요");
    }
}
