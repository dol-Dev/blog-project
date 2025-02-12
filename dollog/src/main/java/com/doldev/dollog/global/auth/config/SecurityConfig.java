package com.doldev.dollog.global.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.CorsFilter;

import com.doldev.dollog.domain.account.snsUser.application.CustomOAuth2UserService;
import com.doldev.dollog.global.auth.filter.CustomAuthenticationFilter;
import com.doldev.dollog.global.auth.filter.CustomLoginFilter;
import com.doldev.dollog.global.auth.service.CustomUserDetailsService;
import com.doldev.dollog.global.auth.service.JwtTokenProvider;
import com.doldev.dollog.global.auth.service.TokenManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtUtil;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenManager tokenManager;
    private final CustomUserDetailsService customUserDetailsService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CorsFilter corsFilter;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // 정책 및 필터 순서 설정
        http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        sessionManagement -> sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(customAuthenticationFilter(), CustomLoginFilter.class)
                .addFilterBefore(customLoginFilter(), UsernamePasswordAuthenticationFilter.class); // CustomAuthenticationFilter

        // 인가 설정
        http.authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests.anyRequest().permitAll());

        // OAuth2 설정
        http.oauth2Login(oauth2Login -> oauth2Login
                .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint.userService(customOAuth2UserService))
                .successHandler((request, response, authentication) -> response.sendRedirect("http://localhost:3000"))); // 추후 리액트 주소로 변경 

        return http.build();
    }

    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private CustomLoginFilter customLoginFilter() throws Exception {
        CustomLoginFilter filter = new CustomLoginFilter(tokenManager, objectMapper);
        filter.setAuthenticationManager(authenticationManager(authenticationConfiguration));
        filter.setFilterProcessesUrl("/api/auth/login"); // Url Mapping
        return filter;
    }

    private CustomAuthenticationFilter customAuthenticationFilter() {
        return new CustomAuthenticationFilter(jwtUtil, tokenManager, customUserDetailsService, redisTemplate,
                objectMapper);
    }
}