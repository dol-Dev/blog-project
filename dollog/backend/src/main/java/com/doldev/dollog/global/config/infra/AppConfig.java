package com.doldev.dollog.global.config.infra;

import java.io.File;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class AppConfig implements WebMvcConfigurer {

    @Value("${avatar.upload-dir}")
    private String avatarUploadDir;

    @Value("${banner.upload-dir}")
    private String bannerUploadDir;

    @Bean
    CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowCredentials(true);
        configuration.addAllowedOrigin("http://localhost:3000"); // local에서 react를 사용할 예정
        configuration.addAllowedHeader("*"); 
        configuration.addAllowedMethod("*");
        configuration.setMaxAge(3600L); 
        source.registerCorsConfiguration("/**", configuration);

        return new CorsFilter(source);
    }

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // 아바타 리소스 핸들러 등록
        String avatarAbsolutePath = new File(avatarUploadDir).getAbsolutePath() + File.separator;
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:" + avatarAbsolutePath);

        // 배너 리소스 핸들러 등록
        String bannerAbsolutePath = new File(bannerUploadDir).getAbsolutePath() + File.separator;
        registry.addResourceHandler("/uploads/banners/**")
                .addResourceLocations("file:" + bannerAbsolutePath);
    }
}
