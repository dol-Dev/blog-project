package com.doldev.dollog.domain.account.profile.service;

import java.io.IOException;

import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.profile.entity.Profile;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileSerivce {

    private final AvatarService avatarService;

    public Profile createProfile(String nickname) {

        String avatarPath;
        try {
            avatarPath = avatarService.createDefaultAvatar(nickname);
        } catch (IOException e) {
            log.error("아바타 생성 실패: {}", e.getMessage());
            throw new RuntimeException("아바타 생성 실패", e);
        }

        return Profile.builder()
                .nickname(nickname)
                .avatarImageUrl(avatarPath)
                .build();
    }
}
