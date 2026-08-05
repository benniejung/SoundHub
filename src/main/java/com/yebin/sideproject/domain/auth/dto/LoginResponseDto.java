package com.yebin.sideproject.domain.auth.dto;

import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.entity.enums.Role;

public record LoginResponseDto(
        String accessToken,
        String refreshToken,
        String email,
        String nickname,
        Role role
) {
}
