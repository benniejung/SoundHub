package com.yebin.sideproject.domain.auth.dto;

import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.entity.enums.Role;

public record SignupResponse(Long id, String email, String nickname, Role role) {

    public static SignupResponse from(User user) {
        return new SignupResponse(user.getId(), user.getEmail(), user.getNickname(), user.getRole());
    }
}
