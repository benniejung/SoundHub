package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.ConfirmDuplicateNicknameRequestDto;
import com.yebin.sideproject.domain.auth.dto.LoginResponseDto;
import com.yebin.sideproject.domain.auth.dto.RefreshRequestDto;
import com.yebin.sideproject.domain.auth.dto.SignupRequestDto;
import com.yebin.sideproject.domain.auth.dto.SignupResponseDto;
import jakarta.validation.Valid;

public interface AuthService {

    SignupResponseDto signup(SignupRequestDto request);

    void confirmDuplicateNickname(ConfirmDuplicateNicknameRequestDto request);

    LoginResponseDto renewAcessToken(@Valid RefreshRequestDto request);

    void logout(String refreshToken);
}
