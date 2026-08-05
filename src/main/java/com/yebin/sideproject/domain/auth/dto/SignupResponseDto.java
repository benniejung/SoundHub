package com.yebin.sideproject.domain.auth.dto;

import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.entity.enums.Role;

public record SignupResponseDto(
        Long id,
        String email,
        String nickname,
        Role role
) {
}
