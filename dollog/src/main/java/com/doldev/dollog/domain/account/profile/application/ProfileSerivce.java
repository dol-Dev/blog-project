package com.doldev.dollog.domain.account.profile.application;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doldev.dollog.domain.account.profile.dto.req.UpdateProfileReqDto;
import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileSerivce {

    private final AvatarService avatarService;
    private final UserRepository userRepository;
    private final SnsUserRepository snsUserRepository;

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

    public void updateProfile(UpdateProfileReqDto req, MultipartFile avatarFile, CustomUserDetails userDetails) {
        try {
            if (userDetails.isUser()) {
                User user = userRepository.findByUsername(userDetails.getUsername())
                        .orElseThrow(() -> new IllegalArgumentException("회원 찾기 실패"));
                applyProfileUpdate(user::getProfile, user::assignProfile, req, avatarFile);
            } else if (userDetails.isSnsUser()) {
                SnsUser snsUser = snsUserRepository
                        .findByUsernameAndProvider(
                                userDetails.getSnsUser().getUsername(),
                                userDetails.getSnsUser().getProvider())
                        .orElseThrow(() -> new IllegalArgumentException("SNS 회원 찾기 실패"));
                applyProfileUpdate(snsUser::getProfile, snsUser::assignProfile, req, avatarFile);
            } else {
                throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
            }
        } catch (IOException e) {
            log.error("아바타 처리 실패", e);
            throw new RuntimeException("아바타 업로드 오류", e);
        }
    }
    
    private void applyProfileUpdate(
            Supplier<Profile> profileGetter, 
            Consumer<Profile> profileSetter,
            UpdateProfileReqDto reqDto,
            MultipartFile avatarFile) throws IOException {
        Profile profile = profileGetter.get();
        avatarService.deleteAvatar(profile.getAvatarImageUrl());
        String avatarPath = avatarService.saveAvatar(reqDto.getNickname(), avatarFile);
        profile.chatAvatarImageUrl(avatarPath);
        profile.changeNickname(reqDto.getNickname());
        profileSetter.accept(profile);
    }
}
