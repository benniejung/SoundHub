package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.ConfirmDuplicateNicknameRequestDto;
import com.yebin.sideproject.domain.auth.dto.LoginRequestDto;
import com.yebin.sideproject.domain.auth.dto.LoginResponseDto;
import com.yebin.sideproject.domain.auth.dto.LoginResult;
import com.yebin.sideproject.domain.auth.dto.SendSmsRequestDto;
import com.yebin.sideproject.domain.auth.dto.SignupRequestDto;
import com.yebin.sideproject.domain.auth.dto.SignupResponseDto;
import com.yebin.sideproject.domain.auth.dto.VerifySMSRequestDto;
import com.yebin.sideproject.domain.auth.dto.VerifySMSResponseDto;

public interface AuthService {

    SignupResponseDto signup(SignupRequestDto request);

    void confirmDuplicateNickname(ConfirmDuplicateNicknameRequestDto request);

    void sendSMS(SendSmsRequestDto request, String clientIp);

    VerifySMSResponseDto verifySMS(VerifySMSRequestDto request);

    LoginResult login(LoginRequestDto request);

    // accessToken이 만료 되어서 refreshToken으로 재발급할 때 요청되는 메서드
    LoginResponseDto renewAcessToken(String refreshToken);

    void logout(String refreshToken);
}
