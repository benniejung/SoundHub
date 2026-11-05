package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.VerifySMSRequestDto;
import com.yebin.sideproject.domain.auth.dto.VerifySMSResponseDto;
import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.domain.auth.repository.SmsAuthRedisRepository;
import com.yebin.sideproject.domain.auth.repository.UserRepository;
import com.yebin.sideproject.global.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplVerifySmsTest {

    private static final String PHONE = "01012345678";
    private static final String CODE = "123456";
    private static final VerifySMSRequestDto CORRECT = new VerifySMSRequestDto(PHONE, CODE);
    private static final VerifySMSRequestDto WRONG = new VerifySMSRequestDto(PHONE, "000000");

    @Mock
    private SmsAuthRedisRepository smsAuthRedisRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void 인증번호가_맞고_미가입_번호이면_코드를_삭제하고_토큰을_발급한다() {
        when(smsAuthRedisRepository.findCode(PHONE)).thenReturn(Optional.of(CODE));
        when(userRepository.existsByPhone(PHONE)).thenReturn(false);

        VerifySMSResponseDto response = authService.verifySMS(CORRECT);

        verify(smsAuthRedisRepository).deleteCode(PHONE);
        verify(smsAuthRedisRepository).saveSignupToken(response.signupToken(), PHONE);
    }

    @Test
    void 이미_가입된_번호이면_토큰을_발급하지_않는다() {
        when(smsAuthRedisRepository.findCode(PHONE)).thenReturn(Optional.of(CODE));
        when(userRepository.existsByPhone(PHONE)).thenReturn(true);

        assertRejectedWith(CORRECT, AuthErrorCode.PHONE_ALREADY_REGISTERED);

        verify(smsAuthRedisRepository).deleteCode(PHONE);
    }

    @Test
    void 인증번호가_없으면_만료_에러가_난다() {
        when(smsAuthRedisRepository.findCode(PHONE)).thenReturn(Optional.empty());

        assertRejectedWith(CORRECT, AuthErrorCode.SMS_CODE_EXPIRED);

        verify(smsAuthRedisRepository, never()).recordFailedAttempt(anyString());
    }

    @Test
    void 인증번호가_틀리면_틀린_횟수를_올리고_가입_여부를_알려주지_않는다() {
        when(smsAuthRedisRepository.findCode(PHONE)).thenReturn(Optional.of(CODE));
        when(smsAuthRedisRepository.recordFailedAttempt(PHONE)).thenReturn(false);

        assertRejectedWith(WRONG, AuthErrorCode.SMS_CODE_MISMATCH);

        verify(userRepository, never()).existsByPhone(anyString());
        verify(smsAuthRedisRepository, never()).deleteCode(anyString());
    }

    @Test
    void 다섯번째로_틀리면_횟수_초과_에러가_난다() {
        when(smsAuthRedisRepository.findCode(PHONE)).thenReturn(Optional.of(CODE));
        when(smsAuthRedisRepository.recordFailedAttempt(PHONE)).thenReturn(true);

        assertRejectedWith(WRONG, AuthErrorCode.SMS_ATTEMPTS_EXCEEDED);
    }

    private void assertRejectedWith(VerifySMSRequestDto request, AuthErrorCode errorCode) {
        BaseException exception = assertThrows(BaseException.class, () -> authService.verifySMS(request));

        assertEquals(errorCode, exception.getErrorCode());
        verify(smsAuthRedisRepository, never()).saveSignupToken(anyString(), anyString());
    }
}
