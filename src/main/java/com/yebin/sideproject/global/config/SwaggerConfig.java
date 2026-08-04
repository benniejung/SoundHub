package com.yebin.sideproject.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        String jwt = "JWT";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(jwt); // 문서 전체에 "JWT"라는 이름의 보안 요구사항을 적용
        Components components = new Components().addSecuritySchemes(jwt, new SecurityScheme() // JWT 토큰 검증할 수 있도록 설정(자물쇠 아이콘 클릭 -> JWT 토큰 입력창 뜸)
                .name(jwt)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
        );
        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components);
    }
    private Info apiInfo() {
        return new Info()
                .title("SoundHub") // API의 제목
                .description("SoundHub Swagger UI") // API에 대한 설명
                .version("1.0.0"); // API의 버전
    }
}
