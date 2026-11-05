package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.SignupRequestDto;
import com.yebin.sideproject.domain.auth.dto.SignupResponseDto;
import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.domain.auth.repository.SmsAuthRedisRepository;
import com.yebin.sideproject.domain.auth.repository.UserRepository;
import com.yebin.sideproject.global.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplSignupTest {

    private static final String EMAIL = "soundhub@gmail.com";
    private static final String TOKEN = "signup-token";
    private static final String PHONE = "01012345678";
    private static final SignupRequestDto REQUEST = new SignupRequestDto(EMAIL, "Password1!", TOKEN);

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SmsAuthRedisRepository smsAuthRedisRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void 회원가입하면_인증된_번호를_저장한_뒤_토큰을_삭제한다() {
        when(smsAuthRedisRepository.findPhoneBySignupToken(TOKEN)).thenReturn(Optional.of(PHONE));
        when(passwordEncoder.encode("Password1!")).thenReturn("encoded");
        when(userRepository.saveAndFlush(any(User.class))).then(returnsFirstArg());

        SignupResponseDto response = authService.signup(REQUEST);

        ArgumentCaptor<User> user = ArgumentCaptor.forClass(User.class);
        InOrder order = inOrder(userRepository, smsAuthRedisRepository);
        order.verify(userRepository).saveAndFlush(user.capture());
        order.verify(smsAuthRedisRepository).deleteSignupToken(TOKEN);

        assertEquals(PHONE, user.getValue().getPhone());
        assertTrue(user.getValue().isPhoneVerified());
        assertTrue(response.nickname().matches("user_[0-9a-f]{8}"));
    }

    @Test
    void 토큰이_없으면_이메일을_검사하지_않고_거부한다() {
        when(smsAuthRedisRepository.findPhoneBySignupToken(TOKEN)).thenReturn(Optional.empty());

        assertRejectedWith(AuthErrorCode.SIGNUP_TOKEN_INVALID);

        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void 이메일이_중복이면_저장하지_않고_토큰을_유지한다() {
        when(smsAuthRedisRepository.findPhoneBySignupToken(TOKEN)).thenReturn(Optional.of(PHONE));
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertRejectedWith(AuthErrorCode.DUPLICATE_EMAIL_ERROR);
    }

    @Test
    void 전화번호가_중복이면_저장하지_않는다() {
        when(smsAuthRedisRepository.findPhoneBySignupToken(TOKEN)).thenReturn(Optional.of(PHONE));
        when(userRepository.existsByPhone(PHONE)).thenReturn(true);

        assertRejectedWith(AuthErrorCode.PHONE_ALREADY_REGISTERED);
    }

    private void assertRejectedWith(AuthErrorCode errorCode) {
        BaseException exception = assertThrows(BaseException.class, () -> authService.signup(REQUEST));

        assertEquals(errorCode, exception.getErrorCode());
        verify(userRepository, never()).saveAndFlush(any(User.class));
        verify(smsAuthRedisRepository, never()).deleteSignupToken(anyString());
    }
}
