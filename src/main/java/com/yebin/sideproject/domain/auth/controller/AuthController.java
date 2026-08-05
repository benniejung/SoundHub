package com.yebin.sideproject.domain.auth.controller;

import com.yebin.sideproject.domain.auth.dto.LoginRequestDto;
import com.yebin.sideproject.domain.auth.dto.LoginResponseDto;
import com.yebin.sideproject.domain.auth.dto.SignupRequestDto;
import com.yebin.sideproject.domain.auth.dto.SignupResponseDto;
import com.yebin.sideproject.domain.auth.service.AuthService;
import com.yebin.sideproject.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "회원가입/로그인", description = "회원가입/로그인 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public SuccessResponse<SignupResponseDto> signup(@Valid @RequestBody SignupRequestDto request) {
        SignupResponseDto data = authService.signup(request);
        return SuccessResponse.created(data);
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
    public ResponseEntity<LoginResponseDto> renewAccessToken(@Valid @RequestBody LoginRequestDto request) {
        LoginResponseDto data = authService.renewAcessToken((request));
        return ResponseEntity.ok(data);
    }

}
