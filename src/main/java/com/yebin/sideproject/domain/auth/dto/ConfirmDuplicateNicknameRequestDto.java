package com.yebin.sideproject.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmDuplicateNicknameRequestDto (
        @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
        @Size(min = 2, max = 20, message = "닉네임은 2자 이상 20자 이하여야 합니다.")
        String nickname
){
}
