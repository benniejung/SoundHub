package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.SignupRequest;
import com.yebin.sideproject.domain.auth.dto.SignupResponse;
import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.exception.DuplicateEmailException;
import com.yebin.sideproject.domain.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupResponse signup(SignupRequest request) {
        // 1. 회원가입 중복 체크: 이미 등록된 이메일이 있는지 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        // 2. 유저 객체 생성 후 DB에 저장 (비밀번호는 해시로 변환해 저장)
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        User saved = userRepository.save(user);
        return SignupResponse.from(saved);
    }

}
