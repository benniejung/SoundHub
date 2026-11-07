package com.yebin.sideproject.domain.auth.dto;

import java.time.Duration;

/**
 * 로그인 서비스 → 컨트롤러 전달용 결과.
 * refreshToken은 응답 바디가 아니라 HttpOnly 쿠키로만 내려가야 하므로 response와 분리한다.
 */
public record LoginResult(
        LoginResponseDto response,
        String refreshToken,
        Duration refreshTokenTtl
) {
}
