package com.yebin.sideproject.global.entity;

import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.global.exception.BaseException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Password {
    public static final int MIN_LENGTH = 8;
    public static final int MAX_LENGTH = 64;
    public static final String STRONG_PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9\\s])\\S+$";
    public static final String STRONG_PASSWORD_MESSAGE = "비밀번호는 8~64자이며 영어 대문자, 소문자, 숫자, 특수문자를 각각 1개 이상 포함하고 공백이 없어야 합니다.";

    private static final int MIN_ID_LENGTH_TO_CHECK = 3; // 너무 짧은 아이디는 우연히 포함될 수 있어 검사하지 않는다
    private static final Set<String> COMMON_PASSWORDS = loadCommonPasswords();

    @Column(name = "password")
    private String passwordNumber;

    private Password(String password) {
        this.passwordNumber = password;
    }

    public static Password savePassword(String rawPassword, String email, PasswordEncoder encoder) {
        validateRawPassword(rawPassword);
        validateNotGuessable(rawPassword, email);
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

    private static void validateNotGuessable(String rawPassword, String email) {
        String lowerPassword = rawPassword.toLowerCase(Locale.ROOT);
        String id = email.split("@")[0].toLowerCase(Locale.ROOT); // 이메일의 @ 앞부분을 아이디로 본다

        if (id.length() >= MIN_ID_LENGTH_TO_CHECK && lowerPassword.contains(id)) {
            throw new BaseException(AuthErrorCode.PASSWORD_CONTAINS_ID);
        }
        if (COMMON_PASSWORDS.contains(lowerPassword)) {
            throw new BaseException(AuthErrorCode.PASSWORD_TOO_COMMON);
        }
    }

    private static Set<String> loadCommonPasswords() {
        try (InputStream in = Password.class.getResourceAsStream("/common-passwords.txt")) {
            return new String(in.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(line -> !line.isBlank())
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public String getEncodedValue() {
        return passwordNumber;
    }
}
