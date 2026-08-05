package com.yebin.sideproject.domain.auth.controller;

import com.yebin.sideproject.domain.auth.dto.LoginRequest;
import com.yebin.sideproject.domain.auth.dto.LoginResponse;
import com.yebin.sideproject.domain.auth.dto.SignupRequest;
import com.yebin.sideproject.domain.auth.dto.SignupResponse;
import com.yebin.sideproject.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
        SignupResponse response = authService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "로그인", description = "이메일/비밀번호로 로그인하여 JWT토큰을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(schema = @Schema(implementation = LoginResponse.class)))
    @ApiResponse(responseCode = "401", description = "이메일 또는 비밀번호가 올바르지 않음")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        throw new IllegalStateException("이 메서드는 실행되지 않습니다. JsonUsernamePasswordAuthenticationFilter가 처리합니다.");
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> generateRefreshToken(@Valid @RequestBody LoginRequest request) {
        throw new IllegalStateException("이 메서드는 실행되지 않습니다. JsonUsernamePasswordAuthenticationFilter가 처리합니다.");
    }

}
