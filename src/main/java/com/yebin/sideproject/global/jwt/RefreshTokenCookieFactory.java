package com.yebin.sideproject.global.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class RefreshTokenCookieFactory {

    private static final String COOKIE_NAME = "refreshToken";

    @Value("${cookie.secure:true}")
    private boolean cookieSecure;

    // 로그인 시 refreshToken을 담아 발급하는 쿠키
    public ResponseCookie issue(String refreshToken, Duration ttl) {
        return build(refreshToken, ttl);
    }

    // 로그아웃 시 클라이언트의 refreshToken 쿠키를 즉시 만료시키는 쿠키
    public ResponseCookie expire() {
        return build("", Duration.ZERO);
    }

    private ResponseCookie build(String value, Duration ttl) {
        return ResponseCookie.from(COOKIE_NAME, value)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "None" : "Lax")
                .path("/")
                .maxAge(ttl)
                .build();
    }
}
