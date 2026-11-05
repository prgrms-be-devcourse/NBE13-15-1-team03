package com.kdt.cafe.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * - localhost:8000은 브라우저에서 다른 Origin이므로 허용하지 않습니다. 주소는 127.0.0.1로 통일.
 * - 쿠키·인증 토큰을 사용하지 않으므로 자격 증명(credentials)은 허용하지 않음.
 */
@Configuration
public class CafeCorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://127.0.0.1:8000")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("Content-Type")
                .allowCredentials(false);
    }
}
