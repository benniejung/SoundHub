package com.yebin.sideproject.domain.auth.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SendSmsRequestDtoTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void 숫자만_입력한_휴대폰_번호는_통과한다() {
        assertTrue(validator.validate(new SendSmsRequestDto("01012345678")).isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "010-1234-5678", "0101234567a", "02012345678", "010123456"})
    void 형식이_잘못된_번호는_거부한다(String phoneNumber) {
        assertFalse(validator.validate(new SendSmsRequestDto(phoneNumber)).isEmpty());
    }
}
