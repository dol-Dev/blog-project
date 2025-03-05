package com.doldev.dollog.global.config.infra;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SuppressWarnings("null")
@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Value("${avatar.upload-dir}")
    private String uploadDir;

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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // uploads/avatars 디렉토리의 절대 경로 추출
        String absolutePath = new File(uploadDir).getAbsolutePath() + File.separator;

        // "/uploads/avatars/**" URL 패턴으로 온 요청을 addResourceLocations에 설정한 디렉토리에서 가져와줌
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + absolutePath);
    }
}
