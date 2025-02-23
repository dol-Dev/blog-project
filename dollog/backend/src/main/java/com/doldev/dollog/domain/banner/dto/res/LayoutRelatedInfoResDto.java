package com.doldev.dollog.domain.banner.dto.res;

import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.banner.entity.Banner;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LayoutRelatedInfoResDto {
    private String bannerImageUrl;
    private String bannerDescription;
    private String blogName;
    private int userId;

    public static LayoutRelatedInfoResDto from(User user) {
        Banner banner = user.getBanner();
        return LayoutRelatedInfoResDto.builder()
                .bannerImageUrl(banner != null ? banner.getBannerImageUrl() : null)
                .bannerDescription(banner != null ? banner.getBannerDescription() : null)
                .blogName(user.getProfile().getBlogName())
                .userId(user.getId())
                .build();
    }
}
