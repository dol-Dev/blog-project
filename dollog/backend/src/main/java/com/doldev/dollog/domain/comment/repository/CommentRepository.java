package com.doldev.dollog.domain.comment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.doldev.dollog.domain.comment.entity.Comment;

public interface CommentRepository extends JpaRepository<Comment, Integer> {

    // 최상위 댓글 조회 (Fetch Join으로 성능 최적화)
    @Query("SELECT c FROM Comment c LEFT JOIN FETCH c.child WHERE c.post.id = :postId AND c.parent IS NULL")
    List<Comment> findByPostIdAndParentIsNull(@Param("postId") int postId);
}