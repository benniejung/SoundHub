package com.yebin.sideproject.global.sms;

import com.solapi.sdk.SolapiClient;
import com.solapi.sdk.message.model.Message;
import com.solapi.sdk.message.service.DefaultMessageService;
import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.global.exception.BaseException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CoolSmsSender implements SmsSender {

    private final DefaultMessageService messageService;
    private final String fromNumber;

    public CoolSmsSender(@Value("${smsApiKey}") String apiKey,
                         @Value("${smsSecretKey}") String secretKey,
                         @Value("${smsFromNumber}") String fromNumber) {
        this.messageService = SolapiClient.INSTANCE.createInstance(apiKey, secretKey);
        this.fromNumber = fromNumber;
    }

    @Override
    public void send(String to, String text) {
        Message message = new Message();
        message.setFrom(fromNumber);
        message.setTo(to);
        message.setText(text);

        try {
            messageService.send(message);
        } catch (Exception e) {
            // 예외 내용에 인증번호가 포함될 수 있어 예외 종류만 남긴다
            log.error("[SMS] 발송 실패: {}", e.getClass().getSimpleName());
            throw new BaseException(AuthErrorCode.SMS_SEND_FAILED);
        }
    }
}
