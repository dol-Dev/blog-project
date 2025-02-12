package com.doldev.dollog.global.auth.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.doldev.dollog.domain.account.user.dto.req.LoginReqDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.auth.service.TokenManager;
import com.doldev.dollog.global.dto.ApiResDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {

    private final TokenManager tokenManager;
    private final ObjectMapper objectMapper;

    @Value("${app.env}")
    private String env;

    // 로그인 요청이 들어왔을 때 JSON 요청 본문을 파싱하여 Authentication 객체를 생성
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        log.info("------attemptAuthentication start------");
        try {
            LoginReqDto loginReqUser = objectMapper.readValue(request.getInputStream(), LoginReqDto.class);
            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    loginReqUser.getUsername(), loginReqUser.getPassword());
            return getAuthenticationManager().authenticate(authRequest);
        } catch (IOException e) {
            throw new RuntimeException("로그인 요청 파싱 실패", e);
        }
    }

    // 로그인 성공 시 토큰 생성, 쿠키 설정, 그리고 성공 응답 전송
    @Override
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
            Authentication authResult) throws IOException {
        log.info("------successfulAuthentication start------");

        // 1. 인증 객체에서 username 추출
        String username = extractUsername(authResult);

        // 2. 토큰 생성
        Map<String, String> tokens = tokenManager.generateTokens(username);

        // 3. HttpOnly 쿠키에 액세스 토큰과 리프레시 토큰 설정
        addTokenCookies(res, tokens);

        // 4. 로그인 성공 메시지 전송 (JSON 형태)
        writeSuccessResponse(res);
    }


    // Authentication 객체에서 username을 추출
    private String extractUsername(Authentication authResult) {
        return ((CustomUserDetails) authResult.getPrincipal()).getUsername();
    }

   // 생성된 토큰들을 쿠키에 넣기
    private void addTokenCookies(HttpServletResponse res, Map<String, String> tokens) {
        ResponseCookie accessTokenCookie = buildCookie("accessToken", tokens.get("accessToken"),
                Duration.ofMinutes(30));
        ResponseCookie refreshTokenCookie = buildCookie("refreshToken", tokens.get("refreshToken"),
                Duration.ofDays(14));

        res.addHeader("Set-Cookie", accessTokenCookie.toString());
        res.addHeader("Set-Cookie", refreshTokenCookie.toString());
    }

    /**
     * 주어진 이름과 값, 만료 시간을 가진 쿠키를 생성.
     * HttpOnly, SameSite, Secure 옵션도 함께 설정.
     */
    private ResponseCookie buildCookie(String name, String token, Duration maxAge) {
        return ResponseCookie.from(name, URLEncoder.encode(token, StandardCharsets.UTF_8))
                .path("/")
                .maxAge(maxAge)
                .httpOnly(true)
                .sameSite("Strict")
                .secure("prod".equals(env))
                .build();
    }

    // 로그인 성공 시 클라이언트에게 JSON 형식의 성공 응답 작성
    private void writeSuccessResponse(HttpServletResponse res) throws IOException {
        String json = objectMapper.writeValueAsString(ApiResDto.builder().messageCode("SUCSESS_LOGIN").build());
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write(json);
    }

    // 인증 실패 시 401 에러 코드를 반환
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest req, HttpServletResponse res,
            AuthenticationException failed) {
        log.error("인증 실패", failed);
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
