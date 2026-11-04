package com.yebin.sideproject.domain.auth.entity;

import com.yebin.sideproject.domain.auth.entity.enums.AuthProvider;
import com.yebin.sideproject.domain.auth.entity.enums.Role;
import com.yebin.sideproject.global.entity.BaseEntity;
import com.yebin.sideproject.global.entity.Password;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(unique = true, length = 50)
    private String email;

    @Column(nullable = false)
    @Embedded
    private Password password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(length = 50)
    private String phone;

    @ColumnDefault("false")
    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified;

    @ColumnDefault("false")
    @Column(name = "phone_verified", nullable = false)
    private boolean phoneVerified;

    @ColumnDefault("'LOCAL'")
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AuthProvider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Builder
    public User(String loginId, Password password, String nickname, Role role, String email, String phone,
                boolean emailVerified, boolean phoneVerified, AuthProvider provider, String providerId) {
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : Role.CREATOR; // role은 사용자가 설정할 수 없기 때문에 null로 들어옴. 그래서 서버가 설정해 줘야함
        this.email = email;
        this.phone = phone;
        this.emailVerified = emailVerified;
        this.phoneVerified = phoneVerified;
        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
    }
}
