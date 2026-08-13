package com.yebin.sideproject.global.jwt;

import com.yebin.sideproject.domain.auth.dto.LoginResponseDto;
import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.repository.RefreshTokenRedisRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final ObjectMapper objectMapper;

    @Value("${cookie.secure:true}")
    private boolean cookieSecure;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 사용자 정보 저장
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = Objects.requireNonNull(userDetails).getUser();
        log.info( "로그인 성공. JWT 발급. username: {}" ,userDetails.getUsername());

        // 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 리프레시 토큰은 Redis 저장소에 저장
        LocalDateTime refreshExpiresAt = jwtTokenProvider.getExpiration(refreshToken);
        Duration ttl = Duration.between(LocalDateTime.now(), refreshExpiresAt);
        refreshTokenRedisRepository.save(user.getId(), refreshToken, ttl);

        // 리프레시 토큰은 JS에서 접근 불가능한 HttpOnly 쿠키로 전달
        // SameSite=None은 Secure 쿠키에서만 허용되므로, HTTP로 띄우는 로컬 환경(cookie.secure=false)에서는 Lax로 낮춘다
        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite(cookieSecure ? "None" : "Lax")
                .path("/")
                .maxAge(ttl)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new LoginResponseDto(accessToken, null, user.getEmail(), user.getNickname(), user.getRole()));
    }
}
