package com.yebin.sideproject.domain.auth.dto;

import com.yebin.sideproject.global.entity.Password;
import jakarta.validation.constraints.*;

public record SignupRequestDto(
        @NotBlank(message = "이메일은 필수 입력 항목입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
        @Size(min = Password.MIN_LENGTH, max = Password.MAX_LENGTH, message = Password.STRONG_PASSWORD_MESSAGE)
        @Pattern(regexp = Password.STRONG_PASSWORD_REGEX, message = Password.STRONG_PASSWORD_MESSAGE)
        String password
) {
}
