package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.LoginRequestDto;
import com.yebin.sideproject.domain.auth.dto.LoginResult;
import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.entity.enums.Role;
import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.domain.auth.exception.AuthException;
import com.yebin.sideproject.domain.auth.repository.RefreshTokenRedisRepository;
import com.yebin.sideproject.global.jwt.CustomUserDetails;
import com.yebin.sideproject.global.jwt.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplLoginTest {

    private static final String EMAIL = "soundhub@gmail.com";
    private static final String PASSWORD = "Xk9#vLm2qR";
    private static final LoginRequestDto REQUEST = new LoginRequestDto(EMAIL, PASSWORD);

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RefreshTokenRedisRepository refreshTokenRedisRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void 로그인에_성공하면_토큰을_발급하고_refreshToken을_Redis에_저장한다() {
        User user = User.builder().email(EMAIL).nickname("user_1234abcd").build();
        ReflectionTestUtils.setField(user, "id", 1L);
        Authentication authenticated = new UsernamePasswordAuthenticationToken(new CustomUserDetails(user), null);

        when(authenticationManager.authenticate(any())).thenReturn(authenticated);
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh");
        when(jwtTokenProvider.getExpiration("refresh")).thenReturn(Instant.now().plus(Duration.ofHours(1)));

        LoginResult result = authService.login(REQUEST);

        ArgumentCaptor<Authentication> request = ArgumentCaptor.forClass(Authentication.class);
        verify(authenticationManager).authenticate(request.capture());
        assertEquals(EMAIL, request.getValue().getPrincipal());
        assertEquals(PASSWORD, request.getValue().getCredentials());

        verify(refreshTokenRedisRepository).save(1L, "refresh", result.refreshTokenTtl());
        assertEquals("access", result.response().accessToken());
        assertEquals(EMAIL, result.response().email());
        assertEquals("user_1234abcd", result.response().nickname());
        assertEquals(Role.CREATOR, result.response().role());
        assertEquals("refresh", result.refreshToken());
        assertTrue(result.refreshTokenTtl().compareTo(Duration.ofMinutes(59)) > 0);
    }

    @Test
    void principal이_CustomUserDetails가_아니면_토큰을_발급하지_않고_실패한다() {
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("not-user-details", null));

        assertThrows(IllegalStateException.class, () -> authService.login(REQUEST));

        verifyNoInteractions(jwtTokenProvider, refreshTokenRedisRepository);
    }

    @Test
    void 인증에_실패하면_LOGIN_FAILED를_던지고_토큰을_발급하지_않는다() {
        // 비밀번호 불일치, 없는 이메일 모두 DaoAuthenticationProvider가 BadCredentialsException으로 던진다
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        AuthException e = assertThrows(AuthException.class, () -> authService.login(REQUEST));

        assertEquals(AuthErrorCode.LOGIN_FAILED, e.getErrorCode());
        verifyNoInteractions(jwtTokenProvider, refreshTokenRedisRepository);
    }
}
