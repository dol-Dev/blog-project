package com.doldev.dollog.domain.account.profile.application;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.doldev.dollog.domain.account.profile.dto.req.BlogNameUpdateReqDto;
import com.doldev.dollog.domain.account.profile.dto.req.NicknameUpdateReqDto;
import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProfileService {

    private final AvatarService avatarService;

    //사용자와 SNS 사용자의 프로필 조회 및 업데이트 로직을 캡슐화하는 내부 인터페이스
    private interface ProfileHolder {
        Profile getProfile();
        void assignProfile(Profile profile);
    }

    // 로그인 방식에 (일반, sns)에 따른 ProfileHolder 세팅(for Update) 
    private ProfileHolder getProfileHolder(CustomUserDetails userDetails) {
        if (userDetails.isUser()) {
            User user = userDetails.getUser();
            return new ProfileHolder() {
                @Override
                public Profile getProfile() {
                    return user.getProfile();
                }
                @Override
                public void assignProfile(Profile profile) {
                    user.assignProfile(profile);
                }
            };
        } else if (userDetails.isSnsUser()) {
            SnsUser snsUser = userDetails.getSnsUser();
            return new ProfileHolder() {
                @Override
                public Profile getProfile() {
                    return snsUser.getProfile();
                }
                @Override
                public void assignProfile(Profile profile) {
                    snsUser.assignProfile(profile);
                }
            };
        } else {
            throw new IllegalArgumentException("유효하지 않은 사용자입니다.");
        }
    }

    // 회원가입 시 프로필 생성
    public Profile createProfile(String nickname) {
        String avatarImageName;
        try {
            avatarImageName = avatarService.createDefaultAvatar(nickname);
        } catch (IOException e) {
            log.error("아바타 생성 실패: {}", e.getMessage());
            throw new RuntimeException("아바타 생성 실패", e);
        }

        return Profile.builder()
                .nickname(nickname)
                .avatarImageName(avatarImageName)
                .blogName(nickname + "의 블로그")
                .build();
    }

    // 닉네임 업데이트
    @Transactional
    public void updateNickname(NicknameUpdateReqDto reqDto, CustomUserDetails userDetails) {
        ProfileHolder profileHolder = getProfileHolder(userDetails);
        Profile profile = profileHolder.getProfile();
        profile.changeNickname(reqDto.getNickname());
        profileHolder.assignProfile(profile);
    }

    // 아바타 업데이트
    @Transactional
    public void updateAvatar(MultipartFile avatarFile, CustomUserDetails userDetails) throws IOException {
        ProfileHolder profileHolder = getProfileHolder(userDetails);
        Profile profile = profileHolder.getProfile();
        
        // 기존 아바타 삭제
        avatarService.deleteAvatar(profile.getAvatarImageName());

        // 새로운 아바타 저장
        String avatarImageName = avatarService.saveAvatar(profile.getNickname(), avatarFile);
        profile.changeAvatarImageName(avatarImageName);
        profileHolder.assignProfile(profile);
    }

    // 블로그이름 업데이트
    @Transactional
    public void updateBlogName(BlogNameUpdateReqDto reqDto, CustomUserDetails userDetails) {
        ProfileHolder profileHolder = getProfileHolder(userDetails);
        Profile profile = profileHolder.getProfile();
        profile.changeBlogName(reqDto.getBlogName());
        profileHolder.assignProfile(profile);
    }
}
