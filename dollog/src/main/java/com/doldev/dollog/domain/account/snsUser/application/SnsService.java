package com.doldev.dollog.domain.account.snsUser.application;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;
import com.doldev.dollog.domain.account.snsUser.enums.SnsType;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.domain.account.snsUser.util.SnsOAuth2Utils;
import com.doldev.dollog.global.auth.service.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsService {
    private final SnsOAuth2Utils snsUtils;
    private final SnsUserRepository snsUserRepository;
    private final AuthenticationService authenticationService;
    private final SnsUserRegistrationService snsUserRegistrationService;

    @Value("${kakao.client.id}")
    private String kakaoClientId;
    @Value("${kakao.key}")
    private String kakaoKey;
    @Value("${naver.client.id}")
    private String naverClientId;
    @Value("${naver.client.secret}")
    private String naverClientSecret;

    public void process(SnsType snsType, String code) {
        try {
            String accessToken = getAccessToken(snsType, code);

            String snsIdentifier = snsUtils.gerSnsIdentifier(accessToken, snsType);

            signupOrLogin(snsIdentifier, snsType);


        } catch (Exception e) {
            log.error("SNS 처리 중 오류 발생: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String getAccessToken(SnsType snsType, String code) {
        return switch (snsType) {
            case KAKAO -> snsUtils.getAccessToken(
                    code, snsType,
                    kakaoClientId, null,
                    "http://localhost:8080/api/oauth2/kakao/callback");
            case NAVER -> snsUtils.getAccessToken(
                    code, snsType,
                    naverClientId, naverClientSecret,
                    "http://localhost:8080/api/oauth2/naver/callback");
        };
    }

    private void signupOrLogin(String snsIdentifier, SnsType snsType) {

        SnsProvider provider = SnsProvider.valueOf(snsType.getProvider());
    
        // 기존 사용자 조회 및 신규 사용자 등록
        SnsUser snsUser = snsUserRepository.findBySnsIdentifierAndProvider(snsIdentifier, provider)
            .orElseGet(() -> {
                // 신규 사용자 등록 후 반환
                return snsUserRegistrationService.registerNewUser(snsIdentifier, provider);
            });
        
        log.info("SNS {} 사용자: {}", snsType, snsUser.getSnsIdentifier());
        
        // 인증 처리
        authenticationService.authenticateSnsUser(snsUser);
    }
}