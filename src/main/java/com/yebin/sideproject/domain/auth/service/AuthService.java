package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.*;
import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.exception.AuthErrorCode;
import com.yebin.sideproject.domain.auth.exception.DuplicateEmailException;
import com.yebin.sideproject.domain.auth.exception.DuplicateNicknameException;
import com.yebin.sideproject.domain.auth.exception.InvalidRefreshTokenException;
import com.yebin.sideproject.domain.auth.repository.RefreshTokenRedisRepository;
import com.yebin.sideproject.domain.auth.repository.UserRepository;
import com.yebin.sideproject.global.jwt.JwtTokenProvider;
import io.jsonwebtoken.JwtException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {
        // 1. 회원가입 중복 체크: 이미 등록된 이메일이 있는지 확인
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(AuthErrorCode.DUPLICATE_EMAIL_ERROR);
        }

        // 2. 닉네임 중복 체크; 이미 등록된 닉네임이 있는지 확인
        if(userRepository.existsByNickname(request.nickname())) {
            throw new DuplicateNicknameException(request.nickname());
        }

        // 3. 유저 객체 생성 후 DB에 저장 (비밀번호는 해시로 변환해 저장)
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();

        User saved = userRepository.save(user);
        return new SignupResponseDto(saved.getId(), saved.getEmail(), saved.getNickname(), saved.getRole());
    }

    public void confirmDuplicateNickname(ConfirmDuplicateNicknameRequestDto request) {
        if(userRepository.existsByNickname(request.nickname())) {
            throw new DuplicateNicknameException(request.nickname());
        }
    }

    // accessToken이 만료 되어서 refreshToken으로 재발급할 때 요청되는 메서드
    @Transactional
    public LoginResponseDto renewAcessToken(@Valid RefreshRequestDto request) {
        // 1. 클라이언트로부터 refreshToken을 받아온다
        String refreshToken = request.refreshToken();

        // 2. 리프레시토큰이 만료되었는지 체크
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new InvalidRefreshTokenException(AuthErrorCode.REFRESH_TOKEN_INVALID); // 만료되면 로그인화면으로 이동(추후 리팩토링)
        }

        // 3. 리프레시토큰으로 유저 아이디 찾기
        Long userId;
        try {
            userId = jwtTokenProvider.getUserId(refreshToken);
        } catch (NumberFormatException e) {
            throw new InvalidRefreshTokenException(AuthErrorCode.REFRESH_TOKEN_INVALID); // 추후 수정
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidRefreshTokenException(AuthErrorCode.REFRESH_TOKEN_INVALID)); // 추후 수정

        // 4. Radis 저장소에 저장되어있던 refreshToken을 가져와서 서로 같은지 확인
        String storedToken = refreshTokenRedisRepository.findByUserId(userId)
                .orElseThrow(() -> new InvalidRefreshTokenException(AuthErrorCode.REFRESH_TOKEN_INVALID)); // 추후 수정

        if (!storedToken.equals(refreshToken)) {
            throw new InvalidRefreshTokenException(AuthErrorCode.REFRESH_TOKEN_INVALID);  // 추후 수정
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        return new LoginResponseDto(newAccessToken, user.getEmail(), user.getNickname(), user.getRole());
    }

    // 로그아웃: Redis에 저장된 refreshToken을 제거한다
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return;
        }

        try {
            Long userId = jwtTokenProvider.getUserId(refreshToken);
            refreshTokenRedisRepository.deleteByUserId(userId);
        } catch (JwtException | NumberFormatException e) {
            // 이미 만료되었거나 위조된 토큰이면 Redis에 삭제할 대상이 없으므로 무시
            log.info("로그아웃 요청에 유효하지 않은 refreshToken이 포함되어 있어 Redis 삭제를 건너뜁니다.");
        }
    }
}
