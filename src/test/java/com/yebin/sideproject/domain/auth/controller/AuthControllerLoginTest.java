package com.yebin.sideproject.domain.auth.controller;

import com.yebin.sideproject.domain.auth.dto.LoginRequestDto;
import com.yebin.sideproject.domain.auth.dto.LoginResponseDto;
import com.yebin.sideproject.domain.auth.dto.LoginResult;
import com.yebin.sideproject.domain.auth.entity.enums.Role;
import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.domain.auth.exception.AuthException;
import com.yebin.sideproject.domain.auth.service.AuthService;
import com.yebin.sideproject.domain.auth.service.UserDetailsServiceImpl;
import com.yebin.sideproject.global.jwt.JwtTokenProvider;
import com.yebin.sideproject.global.jwt.RefreshTokenCookieFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.Duration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(RefreshTokenCookieFactory.class)
class AuthControllerLoginTest {

    private static final String EMAIL = "soundhub@gmail.com";
    private static final String PASSWORD = "Xk9#vLm2qR";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // JwtAuthenticationFilter(@Component)가 슬라이스에 포함되므로 의존성만 채워준다
    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void 로그인에_성공하면_바디에_accessToken을_쿠키에_refreshToken을_담는다() throws Exception {
        LoginResponseDto response = new LoginResponseDto("access", EMAIL, "user_1234abcd", Role.CREATOR);
        when(authService.login(new LoginRequestDto(EMAIL, PASSWORD)))
                .thenReturn(new LoginResult(response, "refresh", Duration.ofHours(1)));

        login(EMAIL, PASSWORD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isSuccess").value(true))
                .andExpect(jsonPath("$.result.accessToken").value("access"))
                .andExpect(jsonPath("$.result.email").value(EMAIL))
                .andExpect(content().string(not(containsString("refresh\""))))
                .andExpect(header().string("Set-Cookie", containsString("refreshToken=refresh")))
                .andExpect(header().string("Set-Cookie", containsString("HttpOnly")))
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=3600")));
    }

    @Test
    void 인증에_실패하면_401과_LOGIN_FAILED를_반환하고_쿠키를_주지_않는다() throws Exception {
        when(authService.login(any())).thenThrow(new AuthException(AuthErrorCode.LOGIN_FAILED));

        login(EMAIL, "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("AUTH_401_2")))
                .andExpect(header().doesNotExist("Set-Cookie"));
    }

    @Test
    void 이메일_형식이_틀리면_400을_반환하고_서비스를_호출하지_않는다() throws Exception {
        login("not-an-email", PASSWORD).andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void 비밀번호가_비어있으면_400을_반환하고_서비스를_호출하지_않는다() throws Exception {
        login(EMAIL, "").andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"%s\",\"password\":\"%s\"}".formatted(email, password)));
    }
}
