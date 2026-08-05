package com.yebin.sideproject.global.security;

import com.yebin.sideproject.domain.auth.dto.LoginResponse;
import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.repository.RefreshTokenRedisRepository;
import com.yebin.sideproject.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 사용자 정보 저장
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        User user = userDetails.getUser();
        log.info( "로그인 성공. JWT 발급. username: {}" ,userDetails.getUsername());

        // 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 리프레시 토큰은 Redis 저장소에 저장
        LocalDateTime refreshExpiresAt = jwtTokenProvider.getExpiration(refreshToken);
        Duration ttl = Duration.between(LocalDateTime.now(), refreshExpiresAt);
        refreshTokenRedisRepository.save(user.getId(), refreshToken, ttl);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), LoginResponse.of(accessToken, refreshToken, user));
    }
}
