package com.doldev.dollog.domain.account.snsUser.application;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.profile.service.ProfileSerivce;
import com.doldev.dollog.domain.account.roletype.enums.RoleType;
import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnsUserRegistrationService {
    private final SnsUserRepository snsUserRepository;
    private final ProfileSerivce profileSerivce;

    public SnsUser registerNewUser(String snsIdentifier, SnsProvider provider) {
        // SNS 사용자 생성
        SnsUser snsUser = SnsUser.builder()
                .snsIdentifier(snsIdentifier)
                .provider(provider)
                .role(RoleType.ROLE_USER)
                .build();

        // 프로필 생성
        String nickname = generateNickname(snsUser);
        Profile profile = profileSerivce.createProfile(nickname);
        snsUser.assignProfile(profile);

        return snsUserRepository.save(snsUser);
    }

    private String generateNickname(SnsUser snsUser) {
        return snsUser.getProvider().name().toLowerCase()
                + "_"
                + UUID.randomUUID().toString().substring(0, 4);
    }
}
