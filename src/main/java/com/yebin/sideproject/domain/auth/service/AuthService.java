package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.ConfirmDuplicateNicknameRequestDto;
import com.yebin.sideproject.domain.auth.dto.LoginResponseDto;
import com.yebin.sideproject.domain.auth.dto.SignupRequestDto;
import com.yebin.sideproject.domain.auth.dto.SignupResponseDto;

public interface AuthService {

    SignupResponseDto signup(SignupRequestDto request);

    void confirmDuplicateNickname(ConfirmDuplicateNicknameRequestDto request);

    // accessToken이 만료 되어서 refreshToken으로 재발급할 때 요청되는 메서드
    LoginResponseDto renewAcessToken(String refreshToken);

    void logout(String refreshToken);
}
