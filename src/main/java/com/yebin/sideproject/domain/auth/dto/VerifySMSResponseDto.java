package com.yebin.sideproject.domain.auth.dto;

public record VerifySMSResponseDto(
        String signupToken // 회원가입 요청에 함께 보내는 1회용 토큰
) {
}
