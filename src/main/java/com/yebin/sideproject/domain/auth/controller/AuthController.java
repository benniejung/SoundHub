package com.yebin.sideproject.domain.auth.controller;

import com.yebin.sideproject.domain.auth.dto.*;
import com.yebin.sideproject.domain.auth.service.AuthService;
import com.yebin.sideproject.global.jwt.RefreshTokenCookieFactory;
import com.yebin.sideproject.global.response.BaseResponse;
import com.yebin.sideproject.global.response.code.GlobalSuccessCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RequestMapping("/api/auth")
@RestController
@RequiredArgsConstructor
@Tag(name = "회원가입/로그인", description = "회원가입/로그인 관련 API")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;


    @PostMapping("/signup")
    @Operation(summary = "회원가입", description = "휴대폰 인증 후 받은 signupToken과 이메일, 비밀번호를 입력받아 회원가입합니다.")
    @ApiResponse(responseCode = "201", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = SignupResponseDto.class)))
    @ApiResponse(responseCode = "400", description = "AUTH_400_3: 휴대폰 인증 만료 또는 유효하지 않은 토큰")
    @ApiResponse(responseCode = "409", description = "AUTH_409_1: 이미 가입한 이메일 / AUTH_409_3: 이미 가입한 휴대폰 번호")
    public ResponseEntity<BaseResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto request) {
        SignupResponseDto data = authService.signup(request);
        return ResponseEntity
                .status(GlobalSuccessCode.SUCCESS_CREATED.getHttpStatus())
                .body(BaseResponse.onSuccess(GlobalSuccessCode.SUCCESS_CREATED, data));
    }

    @PostMapping("/phone/send")
    @Operation(summary = "인증번호 발송", description = "입력받은 전화번호로 6자리 인증번호를 SMS로 발송합니다.")
    @ApiResponse(responseCode = "200", description = "발송 요청 성공")
    @ApiResponse(responseCode = "400", description = "올바른 휴대폰 번호 형식이 아닙니다.")
    @ApiResponse(responseCode = "429", description = "AUTH_429_1: 재발송 간격 미충족 / AUTH_429_2: 요청 횟수 초과")
    @ApiResponse(responseCode = "502", description = "SMS 발송 실패")
    public BaseResponse<Void> sendSMS(@Valid @RequestBody SendSmsRequestDto request, HttpServletRequest servletRequest) {
        authService.sendSMS(request, servletRequest.getRemoteAddr());
        return BaseResponse.onSuccess(GlobalSuccessCode.SUCCESS_OK, null);
    }

    @PostMapping("/phone/verify")
    @Operation(summary = "인증번호 및 가입여부 확인", description = "인증번호를 확인하고, 가입되지 않은 번호이면 회원가입에 사용할 토큰을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "휴대폰 인증 완료")
    @ApiResponse(responseCode = "400", description = "AUTH_400_1: 인증번호 만료 / AUTH_400_2: 인증번호 불일치")
    @ApiResponse(responseCode = "409", description = "이미 가입된 휴대폰 번호입니다.")
    @ApiResponse(responseCode = "429", description = "인증번호를 5회 틀렸습니다.")
    public BaseResponse<VerifySMSResponseDto> verifySMS(@Valid @RequestBody VerifySMSRequestDto request) {
        VerifySMSResponseDto data = authService.verifySMS(request);
        return BaseResponse.onSuccess(GlobalSuccessCode.SUCCESS_OK, data);
    }



    @PostMapping("/login")
    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하여 JWT토큰을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않음")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        throw new IllegalStateException("이 메서드는 실행되지 않습니다. JsonUsernamePasswordAuthenticationFilter가 처리합니다.");
    }

    @PostMapping("/refresh")
    @Operation(summary = "토큰 재발급", description = "refreshToken으로 accessToken과 refreshToken을 재발급받습니다.")
    @ApiResponse(responseCode = "200", description = "재발급 성공",
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 refreshToken")
    public ResponseEntity<LoginResponseDto> renewAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        LoginResponseDto data = authService.renewAcessToken(refreshToken);
        return ResponseEntity.ok(data);
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "Redis의 refreshToken을 제거하고 refreshToken 쿠키를 만료시킵니다.")
    public BaseResponse<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                      HttpServletResponse response) {
        authService.logout(refreshToken);

        ResponseCookie expiredCookie = refreshTokenCookieFactory.expire();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return BaseResponse.onSuccess(GlobalSuccessCode.SUCCESS_OK, null);
    }
}
