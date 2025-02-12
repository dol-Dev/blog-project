package com.doldev.dollog.domain.account.snsUser.application;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.auth.service.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final SnsUserRepository snsUserRepository;
    private final SnsUserRegistrationService snsUserRegistrationService;
    private final AuthenticationService authenticationService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest req) {
        OAuth2User oauth2User = super.loadUser(req);

        SnsProvider provider = SnsProvider.valueOf(
                req.getClientRegistration().getRegistrationId().toUpperCase());

        String snsIdentifier = (String) oauth2User.getAttributes().get("sub");

        return snsUserRepository.findBySnsIdentifierAndProvider(snsIdentifier, provider)
                .map(existingUser -> {
                    log.info("기존 {} 사용자: {}", existingUser.getProvider().name(), existingUser.getSnsIdentifier());
                    return new CustomUserDetails(existingUser, oauth2User.getAttributes());
                })
                .orElseGet(() -> {
                    SnsUser snsUser = snsUserRegistrationService.registerNewUser(snsIdentifier, provider); // 회원가입 진행
                    authenticationService.authenticateSnsUser(snsUser);
                    return new CustomUserDetails(snsUser, oauth2User.getAttributes());
                });
    }
}