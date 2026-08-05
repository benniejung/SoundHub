package com.yebin.sideproject.domain.auth.dto;

import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.entity.enums.Role;

public record LoginResponse(String accessToken, String refreshToken, String tokenType, String email, String nickname, Role role) {

    public static LoginResponse of(String accessToken, String refreshToken, User user) {
        return new LoginResponse(accessToken, refreshToken, "Bearer", user.getEmail(), user.getNickname(), user.getRole());
    }
}
