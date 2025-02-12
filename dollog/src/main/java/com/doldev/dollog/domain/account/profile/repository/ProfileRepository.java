package com.doldev.dollog.domain.account.profile.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.doldev.dollog.domain.account.profile.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Integer> {
    boolean existsByNickname(String nickname);
}