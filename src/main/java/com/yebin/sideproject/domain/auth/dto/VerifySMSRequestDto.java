package com.yebin.sideproject.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifySMSRequestDto(
        @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
        @Pattern(regexp = "^$|^01[016789]\\d{7,8}$", message = "전화번호는 하이픈 없이 숫자만 입력해 주세요. 예) 01012345678")
        String phoneNumber,

        @NotBlank(message = "인증번호는 필수 입력 항목입니다.")
        @Pattern(regexp = "^$|^\\d{6}$", message = "인증번호는 숫자 6자리입니다.")
        String smsNumber
) {
}
