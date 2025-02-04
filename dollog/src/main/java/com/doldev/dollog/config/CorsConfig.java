package com.doldev.dollog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true);

        configuration.addAllowedOrigin("http://localhost:3000"); // local에서 react를 사용할 예정

        configuration.addAllowedHeader("*"); // 모든 헤더 요청 허용
        configuration.addAllowedMethod("*"); // 모든 HTTP 메서드 허용
        configuration.setMaxAge(3600L); // 1시간 동안 캐싱
        source.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(source);
    }
}
