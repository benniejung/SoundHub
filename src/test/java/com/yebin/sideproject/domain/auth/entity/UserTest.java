package com.yebin.sideproject.domain.auth.entity;

import com.yebin.sideproject.domain.auth.entity.enums.AuthProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserTest {

    @Test
    void 값을_지정하면_그대로_저장한다() {
        User user = User.builder()
                .email("yebin@example.com")
                .phone("010-0000-0000")
                .emailVerified(true)
                .phoneVerified(true)
                .provider(AuthProvider.LOCAL)
                .providerId("provider-id")
                .build();

        assertEquals("yebin@example.com", user.getEmail());
        assertEquals("010-0000-0000", user.getPhone());
        assertTrue(user.isEmailVerified());
        assertTrue(user.isPhoneVerified());
        assertEquals("provider-id", user.getProviderId());
    }

    @Test
    void 값을_지정하지_않으면_기본값이_적용된다() {
        User user = User.builder().build();

        assertEquals(AuthProvider.LOCAL, user.getProvider());
        assertFalse(user.isEmailVerified());
        assertFalse(user.isPhoneVerified());
        assertNull(user.getEmail());
        assertNull(user.getPhone());
        assertNull(user.getProviderId());
    }
}
