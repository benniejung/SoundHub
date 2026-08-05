package com.yebin.sideproject.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yebin.sideproject.domain.auth.dto.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

import java.io.IOException;

// 커스텀 필터
@Component
public class JsonUsernamePasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    // JSON 문자열을 Java 객체로 변환
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonUsernamePasswordAuthenticationFilter(AuthenticationManager authenticationManager,
                                                    LoginSuccessHandler successHandler,
                                                    LoginFailureHandler failureHandler) {
        // 일반 로그인 요청이 들어옴 -> Manager에게 전달 -> 자동으로 CustomAuthenticationManager에게 전달
        setFilterProcessesUrl("/api/auth/login");
        setAuthenticationManager(authenticationManager);
        setAuthenticationSuccessHandler(successHandler);
        setAuthenticationFailureHandler(failureHandler);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        try {
            // 클라이언트에서 전달한 JSON 형태의 이메일과 비밀번호를 Java로 변환
            LoginRequestDto loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            // 인증 객체 생성
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password());
            return getAuthenticationManager().authenticate(authentication);
        } catch (IOException e) {
            throw new AuthenticationServiceException("로그인 요청 형식이 올바르지 않습니다.", e);
        }
    }
}
