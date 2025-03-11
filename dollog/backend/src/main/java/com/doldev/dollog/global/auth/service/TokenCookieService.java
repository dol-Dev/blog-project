package com.doldev.dollog.global.auth.service;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenCookieService {

    @Value("${app.env}")
    private String env;

    public Optional<String> extractAccessToken(HttpServletRequest req) {
        return extractToken(req, "accessToken");
    }

    public Optional<String> extractRefreshToken(HttpServletRequest req) {
        return extractToken(req, "refreshToken");
    }

    private Optional<String> extractToken(HttpServletRequest req, String name) {
        return Optional.ofNullable(req.getCookies())
                .flatMap(cookies -> Arrays.stream(cookies)
                        .filter(c -> name.equals(c.getName()))
                        .findFirst()
                        .map(c -> URLDecoder.decode(c.getValue(), StandardCharsets.UTF_8))
                        .map(value -> value.startsWith("Bearer ") ? value.substring(7) : value));
    }

    public void setTokens(HttpServletResponse res, Map<String, String> tokens) {
        res.addHeader("Set-Cookie", buildCookie("accessToken", tokens.get("accessToken"), 1800));
        res.addHeader("Set-Cookie", buildCookie("refreshToken", tokens.get("refreshToken"), 1209600));
    }

    public void clearTokens(HttpServletResponse res) {
        res.addHeader("Set-Cookie", buildCookie("accessToken", "", 0));
        res.addHeader("Set-Cookie", buildCookie("refreshToken", "", 0));
    }

    private String buildCookie(String name, String value, int maxAge) {
        return ResponseCookie.from(name, URLEncoder.encode(value, StandardCharsets.UTF_8))
                .path("/")
                .maxAge(Duration.ofSeconds(maxAge))
                .httpOnly(true)
                .sameSite("Strict")
                .secure("prod".equals(env))
                .build()
                .toString();
    }
}