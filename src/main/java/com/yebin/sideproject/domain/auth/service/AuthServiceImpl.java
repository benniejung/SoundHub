package com.yebin.sideproject.domain.auth.service;

import com.yebin.sideproject.domain.auth.dto.*;
import com.yebin.sideproject.domain.auth.entity.User;
import com.yebin.sideproject.domain.auth.exception.*;
import com.yebin.sideproject.domain.auth.repository.RefreshTokenRedisRepository;
import com.yebin.sideproject.domain.auth.repository.SmsAuthRedisRepository;
import com.yebin.sideproject.domain.auth.repository.UserRepository;
import com.yebin.sideproject.global.entity.Password;
import com.yebin.sideproject.global.exception.BaseException;
import com.yebin.sideproject.global.jwt.CustomUserDetails;
import com.yebin.sideproject.global.jwt.JwtTokenProvider;
import com.yebin.sideproject.global.sms.SmsSender;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
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
    private final AuthenticationManager authenticationManager;

    /**
     * 회원가입
     */
    @Override
    @Transactional
    public SignupResponseDto signup(SignupRequestDto request) {
        // 1. Redis에서 signupToken으로 인증된 전화번호 조회
        String phoneNumber = smsAuthRedisRepository.findPhoneBySignupToken(request.signupToken())
                .orElseThrow(() -> new BaseException(AuthErrorCode.SIGNUP_TOKEN_INVALID));

        // 2. 이메일 중복 검사 -> 다른 이메일로 다시 가입할 수 있도록 토큰은 유지
        if (userRepository.existsByEmail(request.email())) {
            throw new AuthException(AuthErrorCode.DUPLICATE_EMAIL_ERROR);
        }

        // 3. 전화번호 중복 검사 -> 토큰이 유효한 동안 같은 번호로 먼저 가입했을 수 있음
        if (userRepository.existsByPhone(phoneNumber)) {
            throw new BaseException(AuthErrorCode.PHONE_ALREADY_REGISTERED);
        }

        // 4. 유저 저장
        User user = User.builder()
                .email(request.email())
                .password(Password.savePassword(request.password(), request.email(), passwordEncoder))
                .nickname(generateUniqueNickname())
                .phone(phoneNumber)
                .phoneVerified(true)
                .build();

        User saved = userRepository.saveAndFlush(user);

        // 5. 같은 토큰으로 다시 가입할 수 없도록 삭제
        smsAuthRedisRepository.deleteSignupToken(request.signupToken());

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
     * 인증번호 확인
     */
    @Override
    public VerifySMSResponseDto verifySMS(VerifySMSRequestDto request) {
        String phoneNumber = request.phoneNumber();

        // 1. Redis에 저장된 인증번호 조회 -> 없으면 만료되었거나 폐기된 인증번호
        String savedCode = smsAuthRedisRepository.findCode(phoneNumber)
                .orElseThrow(() -> new BaseException(AuthErrorCode.SMS_CODE_EXPIRED));

        // 2. 인증번호 비교 -> 틀리면 틀린 횟수 증가 (5회가 되면 인증번호 폐기)
        if (!savedCode.equals(request.smsNumber())) {
            if (smsAuthRedisRepository.recordFailedAttempt(phoneNumber)) {
                throw new BaseException(AuthErrorCode.SMS_ATTEMPTS_EXCEEDED);
            }
            throw new BaseException(AuthErrorCode.SMS_CODE_MISMATCH);
        }

        // 3. 인증번호를 다시 쓸 수 없도록 삭제
        smsAuthRedisRepository.deleteCode(phoneNumber);

        // 4. 해당 번호로 가입된 사용자가 있는지 확인
        if (userRepository.existsByPhone(phoneNumber)) {
            throw new BaseException(AuthErrorCode.PHONE_ALREADY_REGISTERED);
        }

        // 5. 인증 완료 -> 회원가입 때 인증된 번호를 확인할 수 있도록 1회용 토큰 발급
        String signupToken = UUID.randomUUID().toString();
        smsAuthRedisRepository.saveSignupToken(signupToken, phoneNumber);
        return new VerifySMSResponseDto(signupToken);
    }

    /**
     * 로그인
     */
    @Override
    public LoginResult login(LoginRequestDto request) {
        // 1. 이메일/비밀번호 인증 (없는 이메일도 BadCredentialsException으로 감춰짐)
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (AuthenticationException e) {
            throw new AuthException(AuthErrorCode.LOGIN_FAILED);
        }

        // getPrincipal()은 @Nullable -> null/다른 타입이면 Provider 설정 오류이므로 명확한 예외로 실패
        if (!(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new IllegalStateException("인증 결과의 principal이 CustomUserDetails가 아닙니다.");
        }
        User user = userDetails.getUser();

        // 2. 토큰 발급
        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        // 3. refreshToken은 만료 시각까지만 Redis에 보관
        Duration ttl = Duration.between(Instant.now(), jwtTokenProvider.getExpiration(refreshToken));
        refreshTokenRedisRepository.save(user.getId(), refreshToken, ttl);

        LoginResponseDto response = new LoginResponseDto(accessToken, user.getEmail(), user.getNickname(), user.getRole());
        return new LoginResult(response, refreshToken, ttl);
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
