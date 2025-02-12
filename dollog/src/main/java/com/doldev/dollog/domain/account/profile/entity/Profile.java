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
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column
    private String avatarImageUrl;

    @OneToOne(mappedBy = "profile")
    private User user;

    @OneToOne(mappedBy = "profile")
    private SnsUser snsUser;


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
