package com.yebin.sideproject.global.config;

import com.yebin.sideproject.global.jwt.JsonUsernamePasswordAuthenticationFilter;
import com.yebin.sideproject.global.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JsonUsernamePasswordAuthenticationFilter jsonFilter;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/error").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/api/webhooks/s3-events").permitAll()
                        .anyRequest().authenticated()
                )
                // jsonFilter가 UsernamePasswordAuthenticationFilter 자리를 대체
                .addFilterAt(jsonFilter, UsernamePasswordAuthenticationFilter.class)
                // UsernamePasswordAuthenticationFilter가 실행되기 전에 jwtAuthenticationFilter 실행
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
