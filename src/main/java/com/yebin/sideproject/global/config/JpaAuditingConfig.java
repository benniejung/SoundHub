package com.yebin.sideproject.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// 메인 클래스에 두면 @WebMvcTest 슬라이스에서도 JPA Auditing이 로딩되어 실패하므로 분리
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
