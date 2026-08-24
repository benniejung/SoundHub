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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "회원가입/로그인", description = "회원가입/로그인 관련 API")
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenCookieFactory refreshTokenCookieFactory;

    @GetMapping("/confirm-nickname")
    @Operation(summary = "닉네임 중복 확인")
    @ApiResponse(responseCode = "409", description = "해당 닉네임을 사용할 수 없습니다.")
    public ResponseEntity<BaseResponse<Void>> confirmDuplicateNickname(@Valid @RequestBody ConfirmDuplicateNicknameRequestDto request) {
        authService.confirmDuplicateNickname(request);
        return ResponseEntity
                .status(GlobalSuccessCode.SUCCESS_OK.getStatus())
                .body(BaseResponse.onSuccess(GlobalSuccessCode.SUCCESS_OK, null));
    }

    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임을 입력받아 회원가입합니다.")
    @ApiResponse(responseCode = "201", description = "회원가입 성공",
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "409", description = "이미 가입한 이메일입니다.")
    @ApiResponse(responseCode = "409", description = "해당 닉네임을 사용할 수 없습니다.")
    @PostMapping("/signup")
    public ResponseEntity<BaseResponse<SignupResponseDto>> signup(@Valid @RequestBody SignupRequestDto request) {
        SignupResponseDto data = authService.signup(request);
        return ResponseEntity
                .status(GlobalSuccessCode.SUCCESS_CREATED.getStatus())
                .body(BaseResponse.onSuccess(GlobalSuccessCode.SUCCESS_CREATED, data));
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하여 JWT토큰을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않음")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        throw new IllegalStateException("이 메서드는 실행되지 않습니다. JsonUsernamePasswordAuthenticationFilter가 처리합니다.");
    }

    @Operation(summary = "토큰 재발급", description = "refreshToken으로 accessToken과 refreshToken을 재발급받습니다.")
    @ApiResponse(responseCode = "200", description = "재발급 성공",
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class)))
    @ApiResponse(responseCode = "401", description = "유효하지 않거나 만료된 refreshToken")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> renewAccessToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken) {
        LoginResponseDto data = authService.renewAcessToken(refreshToken);
        return ResponseEntity.ok(data);
    }

    @Operation(summary = "로그아웃", description = "Redis의 refreshToken을 제거하고 refreshToken 쿠키를 만료시킵니다.")
    @PostMapping("/logout")
    public BaseResponse<Void> logout(@CookieValue(value = "refreshToken", required = false) String refreshToken,
                                      HttpServletResponse response) {
        authService.logout(refreshToken);

        ResponseCookie expiredCookie = refreshTokenCookieFactory.expire();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());

        return BaseResponse.onSuccess(GlobalSuccessCode.SUCCESS_OK, null);
    }
}
