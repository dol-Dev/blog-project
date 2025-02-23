package com.doldev.dollog.global.auth.filter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.doldev.dollog.global.auth.service.CookieManager;
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
    private final CookieManager cookieManager;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain) throws IOException, ServletException {
        try {
            processAuthentication(req, res);
            chain.doFilter(req, res);
        } catch (TokenAuthenticationManager.InvalidTokenException | TokenAuthenticationManager.TokenMismatchException e) {
            handleAuthError(res, e);
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    private void processAuthentication(HttpServletRequest req, HttpServletResponse res) {
        Optional<String> accessToken = cookieManager.extractAccessToken(req);

        if (accessToken.isPresent() && tokenAuthenticationManager.isValidToken(accessToken.get())) {
            tokenAuthenticationManager.setAuthenticationForFilter(accessToken.get());
        } else {
            processRefreshToken(req, res);
        }
    }

    private void processRefreshToken(HttpServletRequest req, HttpServletResponse res) {
        Optional<String> refreshToken = cookieManager.extractRefreshToken(req);
        refreshToken.ifPresent(token -> {
            Map<String, String> newTokens = tokenAuthenticationManager.refreshTokens(token);
            cookieManager.setTokens(res, newTokens); // response → res로 수정
            tokenAuthenticationManager.setAuthenticationForFilter(newTokens.get("accessToken"));
        });
    }

    private void handleAuthError(HttpServletResponse res, RuntimeException e) throws IOException {
        cookieManager.clearTokens(res);
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.getWriter().write(objectMapper.writeValueAsString(
                Map.of("error", e.getMessage())));
    }
}