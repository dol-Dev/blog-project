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

    
    // 일반 사용자(user) 관련
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% AND c.user.id = :userId")
    Page<Comment> findByUserContentContaining(String keyword, int userId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN c.user u JOIN u.profile up WHERE up.nickname LIKE %:keyword% AND c.user.id = :userId")
    Page<Comment> findByUserNicknameContaining(String keyword, int userId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN c.user u JOIN u.profile up WHERE (c.content LIKE %:keyword% OR up.nickname LIKE %:keyword%) AND c.user.id = :userId")
    Page<Comment> findByUserContentOrNicknameContaining(String keyword, int userId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.user.id = :userId")
    Page<Comment> findAllByUserId(Pageable pageable, int userId);

    // SNS 사용자(SnsUser) 관련
    @Query("SELECT c FROM Comment c WHERE c.content LIKE %:keyword% AND c.snsUser.id = :snsUserId")
    Page<Comment> findBySnsUserContentContaining(String keyword, int snsUserId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN c.snsUser s JOIN s.profile sp WHERE sp.nickname LIKE %:keyword% AND c.snsUser.id = :snsUserId")
    Page<Comment> findBySnsUserNicknameContaining(String keyword, int snsUserId, Pageable pageable);

    @Query("SELECT c FROM Comment c JOIN c.snsUser s JOIN s.profile sp WHERE (c.content LIKE %:keyword% OR sp.nickname LIKE %:keyword%) AND c.snsUser.id = :snsUserId")
    Page<Comment> findBySnsUserContentOrNicknameContaining(String keyword, int snsUserId, Pageable pageable);

    @Query("SELECT c FROM Comment c WHERE c.snsUser.id = :snsUserId")
    Page<Comment> findAllBySnsUserId(Pageable pageable, int snsUserId);

}