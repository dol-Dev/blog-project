package com.doldev.dollog.domain.account.snsUser.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.profile.application.ProfileService;
import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.roletype.enums.RoleType;
import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SnsUserRegistrationService {
    private final SnsUserRepository snsUserRepository;
    private final ProfileService profileService;

    @Transactional
    public SnsUser registerNewUser(String username, SnsProvider provider) {
        // Profile 생성
        Profile profile = createProfile(provider);

        // 2. SnsUser 생성 및 Profile 연관관계 세팅
        SnsUser snsUser = SnsUser.builder()
                .username(username)
                .provider(provider)
                .role(RoleType.ROLE_USER)
                .profile(profile)
                .build();

        return snsUserRepository.save(snsUser);
    }

    private Profile createProfile(SnsProvider provider) {
        String nickname = generateDefaultNickname(provider);
        return profileService.createProfile(nickname);
    }

    private String generateDefaultNickname(SnsProvider provider) {
        return provider.name().toLowerCase() + "_" + UUID.randomUUID().toString().substring(0, 6);
    }
}