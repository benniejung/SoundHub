package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.SendSmsRequestDto;
import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.domain.auth.repository.SmsAuthRedisRepository;
import com.yebin.sideproject.global.exception.BaseException;
import com.yebin.sideproject.global.sms.SmsSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplSendSmsTest {

    private static final String PHONE = "01012345678";
    private static final String IP = "127.0.0.1";

    @Mock
    private SmsAuthRedisRepository smsAuthRedisRepository;

    @Mock
    private SmsSender smsSender;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void 제한을_통과하면_코드를_저장한_뒤_같은_코드를_발송한다() {
        passAllLimits();

        authService.sendSMS(new SendSmsRequestDto(PHONE), IP);

        ArgumentCaptor<String> code = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> text = ArgumentCaptor.forClass(String.class);
        InOrder order = inOrder(smsAuthRedisRepository, smsSender);
        order.verify(smsAuthRedisRepository).isWithinIpLimit(IP);
        order.verify(smsAuthRedisRepository).tryAcquireCooldown(PHONE);
        order.verify(smsAuthRedisRepository).isWithinDailyLimit(PHONE);
        order.verify(smsAuthRedisRepository).saveCode(eq(PHONE), code.capture());
        order.verify(smsSender).send(eq(PHONE), text.capture());

        assertTrue(code.getValue().matches("\\d{6}"));
        assertTrue(text.getValue().contains(code.getValue()));
    }

    @Test
    void IP_제한을_넘으면_횟수_초과_에러가_나고_재발송_간격을_잡지_않는다() {
        when(smsAuthRedisRepository.isWithinIpLimit(IP)).thenReturn(false);

        assertRejectedWith(AuthErrorCode.SMS_SEND_LIMIT_EXCEEDED);

        verify(smsAuthRedisRepository, never()).tryAcquireCooldown(anyString());
    }

    @Test
    void 재발송_간격_안에_요청하면_재요청_에러가_나고_일일_횟수를_세지_않는다() {
        when(smsAuthRedisRepository.isWithinIpLimit(IP)).thenReturn(true);
        when(smsAuthRedisRepository.tryAcquireCooldown(PHONE)).thenReturn(false);

        assertRejectedWith(AuthErrorCode.SMS_RESEND_TOO_SOON);

        verify(smsAuthRedisRepository, never()).isWithinDailyLimit(anyString());
    }

    @Test
    void 하루_횟수를_넘으면_횟수_초과_에러가_난다() {
        when(smsAuthRedisRepository.isWithinIpLimit(IP)).thenReturn(true);
        when(smsAuthRedisRepository.tryAcquireCooldown(PHONE)).thenReturn(true);
        when(smsAuthRedisRepository.isWithinDailyLimit(PHONE)).thenReturn(false);

        assertRejectedWith(AuthErrorCode.SMS_SEND_LIMIT_EXCEEDED);
    }

    @Test
    void 발송에_실패하면_저장한_코드를_삭제한다() {
        passAllLimits();
        doThrow(new BaseException(AuthErrorCode.SMS_SEND_FAILED)).when(smsSender).send(eq(PHONE), anyString());

        BaseException exception = assertThrows(BaseException.class,
                () -> authService.sendSMS(new SendSmsRequestDto(PHONE), IP));

        assertEquals(AuthErrorCode.SMS_SEND_FAILED, exception.getErrorCode());
        verify(smsAuthRedisRepository).deleteCode(PHONE);
    }

    private void passAllLimits() {
        when(smsAuthRedisRepository.isWithinIpLimit(IP)).thenReturn(true);
        when(smsAuthRedisRepository.tryAcquireCooldown(PHONE)).thenReturn(true);
        when(smsAuthRedisRepository.isWithinDailyLimit(PHONE)).thenReturn(true);
    }

    private void assertRejectedWith(AuthErrorCode errorCode) {
        BaseException exception = assertThrows(BaseException.class,
                () -> authService.sendSMS(new SendSmsRequestDto(PHONE), IP));

        assertEquals(errorCode, exception.getErrorCode());
        verify(smsAuthRedisRepository, never()).saveCode(anyString(), anyString());
        verify(smsSender, never()).send(anyString(), anyString());
    }
}
