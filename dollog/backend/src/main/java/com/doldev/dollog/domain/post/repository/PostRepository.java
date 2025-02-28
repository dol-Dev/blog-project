package com.doldev.dollog.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.doldev.dollog.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {

    // 공통
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword%")
    Page<Post> findByTitleOrContent(String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword%")
    Page<Post> findByTitle(String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword%")
    Page<Post> findByContent(String keyword, Pageable pageable);

    Page<Post> findByCategoryId(int categoryId, Pageable pageable);


    // 일반 사용자(user) 관련
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
    Page<Post> findAllByUserId(Pageable pageable, int userId);

    @Query("SELECT p FROM Post p JOIN p.user u JOIN u.profile up WHERE up.nickname = :nickname")
    Page<Post> findAllByUserNickname(Pageable pageable, String nickname);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% AND p.user.id = :userId")
    Page<Post> findByUserTitleContaining(String keyword, int userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword% AND p.user.id = :userId")
    Page<Post> findByUserContentContaining(String keyword, int userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND p.user.id = :userId")
    Page<Post> findByUserTitleOrContentContaining(String keyword, int userId, Pageable pageable);


    // SNS 사용자(SnsUser) 관련
    @Query("SELECT p FROM Post p WHERE p.snsUser.id = :snsUserId")
    Page<Post> findAllBySnsUserId(Pageable pageable, int snsUserId);

    @Query("SELECT p FROM Post p JOIN p.snsUser s JOIN s.profile sp WHERE sp.nickname = :nickname")
    Page<Post> findAllBySnsUserNickname(Pageable pageable, String nickname);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% AND p.snsUser.id = :snsUserId")
    Page<Post> findBySnsUserTitleContaining(String keyword, int snsUserId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword% AND p.snsUser.id = :snsUserId")
    Page<Post> findBySnsUserContentContaining(String keyword, int snsUserId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND p.snsUser.id = :snsUserId")
    Page<Post> findBySnsUserTitleOrContentContaining(String keyword, int snsUserId, Pageable pageable);
}