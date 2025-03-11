package com.doldev.dollog.global.auth.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.doldev.dollog.global.auth.service.TokenCookieService;
import com.doldev.dollog.global.auth.service.TokenAuthenticationManager;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Builder
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final TokenAuthenticationManager tokenAuthenticationManager;
    private final TokenCookieService tokenCookieService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest req,
            @NonNull HttpServletResponse res,
            @NonNull FilterChain chain) throws IOException, ServletException {
        try {
            processAuthentication(req, res);
            chain.doFilter(req, res);
        } catch (TokenAuthenticationManager.InvalidTokenException
                | TokenAuthenticationManager.TokenMismatchException e) {
            handleAuthError(res, e);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void processAuthentication(HttpServletRequest req, HttpServletResponse res) {
        Optional<String> accessTokenOpt = tokenCookieService.extractAccessToken(req);
        if (accessTokenOpt.isPresent() && tokenAuthenticationManager.isValidToken(accessTokenOpt.get())) {
            tokenAuthenticationManager.setAuthentication(accessTokenOpt.get());
        } else {
            processRefreshToken(req, res);
        }
    }

    private void processRefreshToken(HttpServletRequest req, HttpServletResponse res) {
        Optional<String> refreshTokenOpt = tokenCookieService.extractRefreshToken(req);
        refreshTokenOpt.ifPresent(token -> {
            Map<String, String> newTokens = tokenAuthenticationManager.refreshTokens(token);
            tokenCookieService.setTokens(res, newTokens); // response → res로 수정
            tokenAuthenticationManager.setAuthentication(newTokens.get("accessToken"));
        });
    }

    private void handleAuthError(HttpServletResponse res, RuntimeException e) throws IOException {
        tokenCookieService.clearTokens(res);
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write(objectMapper.writeValueAsString(
                Map.of("error", e.getMessage())));
    }
}