package com.yebin.sideproject.domain.auth.entity;

import com.yebin.sideproject.domain.auth.entity.enums.Role;
import com.yebin.sideproject.global.entity.BaseEntity;
import com.yebin.sideproject.global.entity.Password;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, updatable = false)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    @Embedded
    private Password password;

    @Column(nullable = false)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Builder
    public User(String email, Password password, String nickname, Role role) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.role = role != null ? role : Role.CREATOR; // role은 사용자가 설정할 수 없기 때문에 null로 들어옴. 그래서 서버가 설정해 줘야함
    }
}
