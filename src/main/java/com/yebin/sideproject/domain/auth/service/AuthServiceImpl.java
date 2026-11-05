package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.*;
import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.exception.*;
import com.yebin.sideproject.domain.auth.repository.RefreshTokenRedisRepository;
import com.yebin.sideproject.domain.auth.repository.SmsAuthRedisRepository;
import com.yebin.sideproject.domain.auth.repository.UserRepository;
import com.yebin.sideproject.global.entity.Password;
import com.yebin.sideproject.global.exception.BaseException;
import com.yebin.sideproject.global.jwt.JwtTokenProvider;
import com.yebin.sideproject.global.sms.SmsSender;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SmsAuthRedisRepository smsAuthRedisRepository;
    private final SmsSender smsSender;

    /**
     * 회원가입
     */
    @Override
    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL_ERROR);
        }

        User user = User.builder()
                .email(request.email())
                .password(Password.savePassword(request.password(), passwordEncoder))
                .nickname(generateUniqueNickname())
                .build();

        User saved = userRepository.save(user);
        return new SignupResponseDto(saved.getId(), saved.getEmail(), saved.getNickname(), saved.getRole());
    }

    /**
     * 닉네임 중복 체크
     */
    @Override
    public void confirmDuplicateNickname(ConfirmDuplicateNicknameRequestDto request) {
        if(userRepository.existsByNickname(request.nickname())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_NICKNAME_ERROR);
        }
    }

    /**
     * 인증번호 SMS 발송
     */
    @Override
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // 외부 API를 호출하는 동안 DB 커넥션을 점유하지 않도록 트랜잭션 없이 실행
    public void sendSMS(SendSmsRequestDto request, String clientIp) {
        String phoneNumber = request.phoneNumber();

        // IP 제한에 걸린 요청이 다른 사람 번호의 재발송 간격을 잡지 못하도록 IP를 먼저 확인한다
        if (!smsAuthRedisRepository.isWithinIpLimit(clientIp)) {
            throw new BaseException(AuthErrorCode.SMS_SEND_LIMIT_EXCEEDED);
        }
        if (!smsAuthRedisRepository.tryAcquireCooldown(phoneNumber)) {
            throw new BaseException(AuthErrorCode.SMS_RESEND_TOO_SOON);
        }
        if (!smsAuthRedisRepository.isWithinDailyLimit(phoneNumber)) {
            throw new BaseException(AuthErrorCode.SMS_SEND_LIMIT_EXCEEDED);
        }

        String code = generateSmsCode();
        smsAuthRedisRepository.saveCode(phoneNumber, code);

        try {
            smsSender.send(phoneNumber, "[SoundHub] 인증번호 [%s] *타인에게 절대 노출하지 마세요.(계정 도용 위험)".formatted(code));
        } catch (BaseException e) {
            smsAuthRedisRepository.deleteCode(phoneNumber);
            throw e;
        }
    }

    /**
     *  refreshToken 재발급
     */
    @Override
    public LoginResponseDto renewAcessToken(String refreshToken) {
        // 1. 리프레시토큰이 만료되었는지 체크
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID); // 만료되면 로그인화면으로 이동(추후 리팩토링)
        }

        // 2. 리프레시토큰으로 유저 아이디 찾기
        Long userId;
        try {
            userId = jwtTokenProvider.getUserId(refreshToken);
        } catch (NumberFormatException e) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID); // 추후 수정
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID)); // 추후 수정

        // 3. Redis 저장소에 저장되어있던 refreshToken을 가져와서 서로 같은지 확인
        String storedToken = refreshTokenRedisRepository.findByUserId(userId)
                .orElseThrow(() -> new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID)); // 추후 수정

        if (!storedToken.equals(refreshToken)) {
            throw new AuthException(AuthErrorCode.REFRESH_TOKEN_INVALID);  // 추후 수정
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        return new LoginResponseDto(newAccessToken, user.getEmail(), user.getNickname(), user.getRole());
    }

    /**
     *  로그아웃
     */
    @Override
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

    // 랜덤 닉네임 생성 메서드
    private static final int NICKNAME_MAX_RETRY = 5;

    private String generateUniqueNickname() {
        for (int i = 0; i < NICKNAME_MAX_RETRY; i++) {
            String nickname = "user_" + UUID.randomUUID().toString().substring(0, 8);
            if (!userRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
        throw new AuthException(AuthErrorCode.DUPLICATE_NICKNAME_ERROR);
    }

    // 인증코드 생성 메서드
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private String generateSmsCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

}
