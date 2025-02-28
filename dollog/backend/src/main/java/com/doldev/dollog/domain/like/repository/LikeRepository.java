package com.doldev.dollog.domain.like.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.doldev.dollog.domain.like.entity.Like;

public interface LikeRepository extends JpaRepository<Like, Integer> {
    // 일반 로그인 사용자
    Optional<Like> findByPostIdAndUserId(int postId, int userId);

    Optional<Like> findByCommentIdAndUserId(int commentId, int userId);

    // sns 로그인 사용자
    Optional<Like> findByPostIdAndSnsUserId(int postId, int snsUserId);

    Optional<Like> findByCommentIdAndSnsUserId(int commentId, int snsUserId);
}