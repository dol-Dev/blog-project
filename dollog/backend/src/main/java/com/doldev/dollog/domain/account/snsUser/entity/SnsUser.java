package com.doldev.dollog.domain.account.snsUser.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.doldev.dollog.domain.account.profile.entity.Profile;
import com.doldev.dollog.domain.account.roletype.enums.RoleType;
import com.doldev.dollog.domain.account.snsUser.enums.SnsProvider;
import com.doldev.dollog.domain.banner.entity.Banner;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class)
@Entity
public class SnsUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleType role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SnsProvider provider;

    // 각 SNS에서 제공하는 사용자 식별자 ID (User 엔티티의 username 역할)
    @Column(nullable = false)
    private String username;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "banner_id")
    private Banner banner;

    @CreationTimestamp
    private LocalDateTime createDate;

    @UpdateTimestamp
    private LocalDateTime updateDate;

    // 탈퇴
    @Column(name = "withdraw_requested_at")
    private LocalDateTime withdrawReqAt;

    @Column(name = "withdraw_status")
    private boolean withdrawStatus;

    /* 연관관계 설정 메서드들 */
    public void assignProfile(Profile profile) {
        this.profile = profile;
        if (profile.getSnsUser() != this) {
            profile.assignSnsUser(this);
        }
    }

    public void assignBanner(Banner banner) {
        this.banner = banner;
        if (banner.getSnsUser() != this) {
            banner.assignSnsUser(this);
        }
    }

    /* 탈퇴 관련 메서드들 */
    public void setWithdrawReqAt(LocalDateTime withdrawReqAt) {
        this.withdrawReqAt = withdrawReqAt;
    }

    public void setWithdrawStatus(boolean withdrawStatus) {
        this.withdrawStatus = withdrawStatus;
    }
}
