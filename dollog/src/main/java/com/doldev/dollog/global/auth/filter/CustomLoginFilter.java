package com.doldev.dollog.global.auth.filter;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.doldev.dollog.domain.user.dto.req.LoginReqDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.auth.service.JwtTokenProvider;
import com.doldev.dollog.global.auth.service.TokenManager;
import com.doldev.dollog.global.dto.ApiResDto;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CustomLoginFilter extends UsernamePasswordAuthenticationFilter {
    private final TokenManager tokenManager;

    @Override // 로그인 요청 처리
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        log.info("------attemptAuthentication start------");

        ObjectMapper om = new ObjectMapper();
        try {
            LoginReqDto loginReqUser = om.readValue(request.getInputStream(), LoginReqDto.class);
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(loginReqUser.getUsername(), loginReqUser.getPassword()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override // 로그인 성공 처리
    protected void successfulAuthentication(HttpServletRequest req, HttpServletResponse res, FilterChain chain,
            Authentication authResult) throws IOException {
        log.info("------successfulAuthentication start------");

        // username 받아오기
        String username = ((CustomUserDetails) authResult.getPrincipal()).getUsername();

        // AccessToken과 RefreshToken 생성
        String accessToken = tokenManager.generateTokens(username);

        String encodedValue = URLEncoder.encode(accessToken, StandardCharsets.UTF_8).replace("+", "%20");
        Cookie cookie = new Cookie(JwtTokenProvider.AUTHORIZATION_HEADER, encodedValue);
        cookie.setPath("/");
        cookie.setMaxAge(7200); // 리프레시 토큰의 시간과 똑같게 설정

        res.addCookie(cookie);
        res.setContentType("application/json");
        res.setCharacterEncoding("UTF-8");

        // Serialization ( ApiResponse : Object -> json)
        String json = new ObjectMapper().writeValueAsString(ApiResDto.builder().messageCode("SUCSESS LOGIN").build());
        res.getWriter().write(json);// getWriter().write()는 문자열을 Args로 받으므로 Serialization이 필수
    }

    @Override // 인증 실패 시 401에러 반환
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException failed) {
        log.error("인증 실패", failed);
        response.setStatus(401);
    }
}
