package com.doldev.dollog.domain.banner.entity;

import org.springframework.data.annotation.Id;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.user.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
public class Banner {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String bannerImageUrl;

    private String bannerDescription;

    @OneToOne(mappedBy = "banner")
    private User user;

    @OneToOne(mappedBy = "banner")
    private SnsUser snsUser;

    /* 배너 정보 변경 메서드들 */
    public void changeBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public void changeBannerDescription(String bannerDescription) {
        this.bannerDescription = bannerDescription;
    }

    /* 연관관계 설정 메서드들 */
    public void assignUser(User user) {
        this.user = user;
        if (user.getBanner() != this) {
            user.assignBanner(this);
        }
    }

    public void assignSnsUser(SnsUser snsUser) {
        this.snsUser = snsUser;
        if (snsUser.getBanner() != this) {
            snsUser.assignBanner(this);
        }
    }
}
