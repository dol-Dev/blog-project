package com.doldev.dollog.domain.account.snsUser.service;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.auth.service.TokenCookieService;
import com.doldev.dollog.global.auth.service.TokenService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String SNS_IDENTIFIER_ATTRIBUTE = "sub";

    private final SnsUserRepository snsUserRepository;
    private final SnsUserRegistrationService snsUserRegistrationService;
    private final TokenService tokenService;
    private final TokenCookieService tokenCookieService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oauth2User = super.loadUser(req);

        SnsProvider provider = SnsProvider.valueOf(
                req.getClientRegistration().getRegistrationId().toUpperCase());

        String username = (String) oauth2User.getAttributes().get(SNS_IDENTIFIER_ATTRIBUTE);

        return snsUserRepository.findByUsernameAndProvider(username, provider)
                .map(existingUser -> handleExistingUser(existingUser, oauth2User))
                .orElseGet(() -> handleNewUser(username, provider, oauth2User));
    }

    // 기존 사용자 
    private CustomUserDetails handleExistingUser(SnsUser snsUser, OAuth2User oauth2User) {
        if (snsUser.isWithdrawStatus()) {
            OAuth2Error oAuth2Error = new OAuth2Error(
                    "account_disabled",
                    "ACCOUNT_DISABLED:" + snsUser.getUsername(),
                    null);
            throw new OAuth2AuthenticationException(oAuth2Error, "ACCOUNT_DISABLED:" + snsUser.getUsername());
        }
        applyTokens(snsUser);
        return new CustomUserDetails(snsUser, oauth2User.getAttributes());
    }

    // 신규 사용자 
    private CustomUserDetails handleNewUser(String snsId, SnsProvider provider, OAuth2User oauth2User) {
        SnsUser newUser = registerAndAuthenticateUser(
                () -> snsUserRegistrationService.registerNewUser(snsId, provider));
        return new CustomUserDetails(newUser, oauth2User.getAttributes());
    }

    private SnsUser registerAndAuthenticateUser(Supplier<SnsUser> registrationStrategy) {
        SnsUser newUser = registrationStrategy.get();
        setAuthenticationGoogleUser(newUser);
        applyTokens(newUser);
        return newUser;
    }

    // 토큰 생성 및 쿠키 설정
    private void applyTokens(SnsUser user) {
        Map<String, String> tokens = tokenService.generateNewTokens(user.getUsername());
        HttpServletResponse res = getCurrentResponse();
        if (res != null) {
            tokenCookieService.setTokens(res, tokens);
        } else {
            log.warn("HttpServletResponse is not available in the current context.");
        }
    }

    private HttpServletResponse getCurrentResponse() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attrs != null ? attrs.getResponse() : null;
    }

    public void setAuthenticationGoogleUser(SnsUser snsUser) {
        Collection<? extends GrantedAuthority> authorities = Collections
                .singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken(
                snsUser.getUsername(),
                null,
                authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}
