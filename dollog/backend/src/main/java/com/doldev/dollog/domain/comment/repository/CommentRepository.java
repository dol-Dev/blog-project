package com.doldev.dollog.domain.comment.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.doldev.dollog.domain.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // 최상위 댓글 조회
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.child WHERE c.post.id = :postId AND c.parent IS NULL")
    List<Comment> findByPostIdAndParentIsNull(@Param("postId") int postId);

    // 일반 User의 id로 최상위 댓글 조회
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.child WHERE c.user.id = :userId AND c.parent IS NULL")
    Page<Comment> findByUserIdAndParentIsNull(@Param("userId") int userId, Pageable pageable);

    // SNS User의 id로 최상위 댓글 조회
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.child WHERE c.snsUser.id = :snsUserId AND c.parent IS NULL")
    Page<Comment> findBySnsUserIdAndParentIsNull(@Param("snsUserId") int snsUserId, Pageable pageable);

}