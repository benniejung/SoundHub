package com.yebin.sideproject.global.entity;

import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.global.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PasswordTest {

    private static final String EMAIL = "soundhub@gmail.com";

    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);

    @Test
    void 규칙을_모두_만족하면_암호화해서_저장한다() {
        assertNotNull(Password.savePassword("Xk9#vLm2qR", EMAIL, encoder).getEncodedValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"xk9#vlm2qr", "XK9#VLM2QR"})
    void 대문자나_소문자가_없으면_거부한다(String rawPassword) {
        assertThrows(IllegalArgumentException.class, () -> Password.savePassword(rawPassword, EMAIL, encoder));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Soundhub9#x", "xSOUNDHUB9#", "9#SoUnDhUbx"})
    void 아이디를_포함하면_대소문자_구분_없이_거부한다(String rawPassword) {
        assertRejectedWith(rawPassword, EMAIL, AuthErrorCode.PASSWORD_CONTAINS_ID);
    }

    @Test
    void 너무_짧은_아이디는_포함_여부를_검사하지_않는다() {
        assertNotNull(Password.savePassword("Xk9#vLm2qR", "xk@gmail.com", encoder).getEncodedValue());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Password1!", "pAsSwOrD1!", "P@ssw0rd1!", "Qwerty123!"})
    void 흔한_비밀번호는_대소문자_구분_없이_거부한다(String rawPassword) {
        assertRejectedWith(rawPassword, "user@gmail.com", AuthErrorCode.PASSWORD_TOO_COMMON);
    }

    private void assertRejectedWith(String rawPassword, String email, AuthErrorCode errorCode) {
        BaseException exception = assertThrows(BaseException.class, () -> Password.savePassword(rawPassword, email, encoder));

        assertEquals(errorCode, exception.getErrorCode());
    }
}
