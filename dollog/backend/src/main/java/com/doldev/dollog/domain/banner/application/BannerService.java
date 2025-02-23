package com.doldev.dollog.domain.banner.application;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.doldev.dollog.domain.account.profile.repository.ProfileRepository;
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
    private final ProfileRepository profileRepository;

    public User createBanner(CustomUserDetails userDetails, BannerReqDto reqDto) {
        Optional<User> userOptional = userRepository.findById(userDetails.getUser().getId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            Banner banner = userOptional.get().getBanner();
            Banner.builder()
                    .bannerDescription(reqDto.getBannerDescription())
                    .bannerImageUrl(reqDto.getBannerImageUrl())
                    .build();

            user.assignBanner(banner);

            userRepository.save(user);
            return user;
        }
        return null;
    }

    public LayoutRelatedInfoResDto findLayoutInfoByPrincipal(CustomUserDetails userDetails) {
        return userRepository.findById(userDetails.getUser().getId())
                .map(LayoutRelatedInfoResDto::from)
                .orElse(null);
    }

    public LayoutRelatedInfoResDto findLayoutInfoByNickname(String nickname) {
        return profileRepository.findByNickname(nickname)
                .map(profile -> LayoutRelatedInfoResDto.from(profile.getUser()))
                .orElse(null);
    }
}