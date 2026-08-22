package com.yebin.sideproject.global.jwt;

import com.yebin.sideproject.domain.auth.entity.User;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpirationMs = 15 * 60 * 1000;      // 15분 (900,000ms)
    private final long refreshExpirationMs = 60 * 60 * 1000;     // 1시간 (3,600,000ms)

    public JwtTokenProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    // 1. accessToken 생성 메서드
    public String generateAccessToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(user.getEmail()) // 표준 클레임 - 이 토큰이 누구에 대한 것인지
                .claim("role", user.getRole().name()) // 커스텀 클레임 - 권한
                .claim("nickname", user.getNickname()) // 커스텀 클레임 - 권한
                .setIssuedAt(now) // 토큰 발급 시각
                .setExpiration(new Date(now.getTime() + accessExpirationMs)) // 토큰 만료 시각
                .signWith(key, SignatureAlgorithm.HS256) // 토큰 서명
                .compact();
    }

    // 2. refreshToken 생성 메서드
    // accessToken과 달리 최소한의 정보만 저장한다
    public String generateRefreshToken(User user) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(user.getId().toString())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + refreshExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // 3. 토큰 검증 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public LocalDateTime getExpiration(String token) {
        Date expiration = Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
        return LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());
    }

    public String getEmail(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // refreshToken의 subject는 이메일이 아니라 userId이므로 별도 메서드로 파싱
    public Long getUserId(String token) {
        return Long.parseLong(getEmail(token));
    }
}
