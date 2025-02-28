package com.doldev.dollog.domain.banner.application;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.profile.repository.ProfileRepository;
import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.domain.banner.dto.req.BannerReqDto;
import com.doldev.dollog.domain.banner.dto.res.LayoutRelatedInfoResDto;
import com.doldev.dollog.domain.banner.entity.Banner;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final UserRepository userRepository;
    private final SnsUserRepository snsUserRepository;
    private final ProfileRepository profileRepository;

    public void createBanner(CustomUserDetails userDetails, BannerReqDto reqDto) {
        Banner banner = Banner.builder()
                .bannerDescription(reqDto.getBannerDescription())
                .bannerImageUrl(reqDto.getBannerImageUrl())
                .build();

        Optional<Profile> profileOpt = profileRepository.findByNickname(userDetails.getNickname());
        if (profileOpt.isPresent()) {
            if (userDetails.getUser() != null) {
                User user = userDetails.getUser();
                user.assignBanner(banner);
                userRepository.save(user);
            } else if (userDetails.isSnsUser()) {
                SnsUser snsUser = userDetails.getSnsUser();
                snsUser.assignBanner(banner);
                snsUserRepository.save(snsUser);
            }
        }
    }

    public LayoutRelatedInfoResDto findLayoutInfoByPrincipal(CustomUserDetails userDetails) {
        if (userDetails.isUser()) {
            return userRepository.findById(userDetails.getId())
                    .map(LayoutRelatedInfoResDto::fromEntity)
                    .orElse(null);
        } else if (userDetails.isSnsUser()) {
            return snsUserRepository.findById(userDetails.getId())
                    .map(LayoutRelatedInfoResDto::fromEntity)
                    .orElse(null);
        }
        return null;
    }

    public LayoutRelatedInfoResDto findLayoutInfoByNickname(String nickname) {
        Optional<Profile> profile = profileRepository.findByNickname(nickname);
        if (profile.isPresent()) {
            if (profile.get().getUser() != null) {
                return LayoutRelatedInfoResDto.fromEntity(profile.get().getUser());
            } else if (profile.get().getSnsUser() != null) {
                return LayoutRelatedInfoResDto.fromEntity(profile.get().getSnsUser());
            }
        }
        return null;
    }
}