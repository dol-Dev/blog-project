package com.doldev.dollog.domain.account.profile.entity;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    private String blogName;

    private String avatarImageUrl;

    @OneToOne(mappedBy = "profile")
    private User user;

    @OneToOne(mappedBy = "profile")
    private SnsUser snsUser;

    /* 프로필 정보 변경 메서드들 */
    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeAvatarImageUrl(String avatarImageUrl) {
        this.avatarImageUrl = avatarImageUrl;
    }

    public void changeBlogName(String blogName) {
        this.blogName = blogName;
    }

    /* 연관관계 설정 메서드들 */
    public void assignUser(User user) {
        this.user = user;
        if (user.getProfile() != this) {
            user.assignProfile(this);
        }
    }

    public void assignSnsUser(SnsUser snsUser) {
        this.snsUser = snsUser;
        if (snsUser.getProfile() != this) {
            snsUser.assignProfile(this);
        }
    }
}
