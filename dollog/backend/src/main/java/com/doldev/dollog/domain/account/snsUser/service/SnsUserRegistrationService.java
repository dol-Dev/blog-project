package com.doldev.dollog.domain.account.snsUser.service;

import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.profile.application.ProfileService;
import com.doldev.dollog.domain.account.profile.entity.Profile;
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
    private Function<SnsUser, String> nicknameGenerator = this::generateDefaultNickname;
    private Consumer<Profile> additionalProfileSetup = profile -> {
    };
    private final SnsUserRepository snsUserRepository;
    private final ProfileService profileSerivce;

    public SnsUser registerNewUser(String username, SnsProvider provider) {
        SnsUser snsUser = buildSnsUser(username, provider);
        Profile profile = createProfileWithStrategy(snsUser);
        snsUser.assignProfile(profile);
        return snsUserRepository.save(snsUser);
    }

    private SnsUser buildSnsUser(String id, SnsProvider provider) {
        return SnsUser.builder()
                .username(id)
                .provider(provider)
                .role(RoleType.ROLE_USER)
                .build();
    }

    private Profile createProfileWithStrategy(SnsUser user) {
        String nickname = nicknameGenerator.apply(user);
        Profile profile = profileSerivce.createProfile(nickname);
        additionalProfileSetup.accept(profile);
        return profile;
    }

    private String generateDefaultNickname(SnsUser user) {
        return user.getProvider().name().toLowerCase()
                + "_"
                + UUID.randomUUID().toString().substring(0, 4);
    }
}
