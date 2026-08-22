package com.yebin.sideproject.global.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Password {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;
    public static final String STRONG_PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S+$";
    public static final String STRONG_PASSWORD_MESSAGE = "비밀번호는 8~64자이며 영문자, 숫자, 특수문자를 각각 1개 이상 포함하고 공백이 없어야 합니다.";

    @Column(name = "password")
    private String password;

    private Password(String password) {
        this.password = password;
    }

    public static Password savePassword(String rawPassword, PasswordEncoder encoder) {
        validateRawPassword(rawPassword);
        return new Password(encoder.encode(rawPassword));
    }

    private static void validateRawPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException(STRONG_PASSWORD_MESSAGE);
        }
        if (rawPassword.length() < MIN_LENGTH || rawPassword.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(STRONG_PASSWORD_MESSAGE);
        }
        if (!rawPassword.matches(STRONG_PASSWORD_REGEX)) {
            throw new IllegalArgumentException(STRONG_PASSWORD_MESSAGE);
        }
    }

    public String getEncodedValue() {
        return password;
    }
}
