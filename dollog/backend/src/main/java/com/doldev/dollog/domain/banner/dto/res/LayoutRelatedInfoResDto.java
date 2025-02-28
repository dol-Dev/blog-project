package com.doldev.dollog.domain.banner.dto.res;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.banner.entity.Banner;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Builder
@Getter
public class LayoutRelatedInfoResDto {
    private String bannerImageUrl;
    private String bannerDescription;
    private String blogName;
    private String nickname;

    public static LayoutRelatedInfoResDto fromEntity(User user) {
        Banner banner = user.getBanner();
        return LayoutRelatedInfoResDto.builder()
                .bannerImageUrl(banner != null ? banner.getBannerImageUrl() : null)
                .bannerDescription(banner != null ? banner.getBannerDescription() : null)
                .blogName(user.getProfile().getBlogName())
                .nickname(user.getProfile().getNickname())
                .build();
    }

    public static LayoutRelatedInfoResDto fromEntity(SnsUser snsUser) {
        Banner banner = snsUser.getBanner();
        return LayoutRelatedInfoResDto.builder()
                .bannerImageUrl(banner != null ? banner.getBannerImageUrl() : null)
                .bannerDescription(banner != null ? banner.getBannerDescription() : null)
                .blogName(snsUser.getProfile().getBlogName())
                .nickname(snsUser.getProfile().getNickname())
                .build();
    }
}
