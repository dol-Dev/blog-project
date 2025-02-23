package com.doldev.dollog.domain.post.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.doldev.dollog.domain.post.entity.Post;

public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query("SELECT p FROM Post p WHERE p.title LIKE %?1% OR p.content LIKE %?1%")
    Page<Post> findByTitleOrContent(String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.title LIKE %?1%")
    Page<Post> findByTitle(String keyword, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.content LIKE %?1%")
    Page<Post> findByContent(String keyword, Pageable pageable);

    Page<Post> findByCategoryId(int categoryId, Pageable pageable);

    Page<Post> findAllByUserId(Pageable pageable, int userId);

    @Query("SELECT p FROM Post p JOIN p.user u JOIN u.profile pr WHERE pr.nickname = ?1")
    Page<Post> findAllByNickname(Pageable pageable, String nickname);
    
    @Query("SELECT p FROM Post p WHERE p.title LIKE %:keyword% AND p.user.id = :userId")
    Page<Post> findByTitleContainingAndUserId(String keyword, int userId,
                    Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.content LIKE %:keyword% AND p.user.id = :userId")
    Page<Post> findByContentContainingAndUserId(String keyword, int userId,
                    Pageable pageable);

    @Query("SELECT p FROM Post p WHERE (p.title LIKE %?1% OR p.content LIKE %?1%) AND p.user.id = ?2")
    Page<Post> findByTitleContainingOrContentContainingAndUserId(String keyword, int userId, Pageable pageable);

}
