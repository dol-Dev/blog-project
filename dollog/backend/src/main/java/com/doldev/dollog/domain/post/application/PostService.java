package com.doldev.dollog.domain.post.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.domain.category.application.CategoryService;
import com.doldev.dollog.domain.category.entity.Category;
import com.doldev.dollog.domain.post.dto.req.PostCreateReqDto;
import com.doldev.dollog.domain.post.dto.req.PostUpdateReqDto;
import com.doldev.dollog.domain.post.dto.res.PostResDto;
import com.doldev.dollog.domain.post.entity.Post;
import com.doldev.dollog.domain.post.repository.PostRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final CategoryService categoryService;

     // 게시글 생성
    @Transactional
    public void createPost(PostCreateReqDto reqDto,
            CustomUserDetails userDetails) {

        // 카테고리 조회
        Category category = categoryService.findCategoryById(reqDto.getCategoryId());

        Post post = Post.builder()
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .category(category)
                .user(userDetails.getUser())
                .count(0) // 조회수 초기화
                .build();

        postRepository.save(post);
    }

    // 게시글 수정
    @Transactional
    public void updatePost(int postId, PostUpdateReqDto reqDto) {
        Post updatePost = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글 찾기 실패 ID: " + postId));

        // 타이틀, 콘텐츠 업데이트
        updatePost.updateTitle(reqDto.getTitle());
        updatePost.updateContent(reqDto.getContent());
    }

    // 게시글 삭제
    @Transactional
    public void deletePost(int postId) {
        postRepository.deleteById(postId);
    }

    // 특정 게시글 조회
    @Transactional(readOnly = true)
    public PostResDto getPostByPostId(int postId) {
        return postRepository.findById(postId)
                .map(PostResDto::fromEntity)
                .orElseThrow(() -> new IllegalArgumentException("게시글 찾기 실패 ID: " + postId));
    }

    // 페이징된 게시글 전체 조회
    @Transactional(readOnly = true)
    public Page<PostResDto> getAllPosts(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(PostResDto::fromEntity);
    }

    // 페이징된 닉네임에 따른 게시글 조회
    @Transactional(readOnly = true)
    public Page<PostResDto> getPostsByNickname(Pageable pageable, String nickname) {
        return postRepository.findAllByNickname(pageable, nickname)
                .map(PostResDto::fromEntity);
    }

    // 페이징된 카테고리에 따른 게시글 조회
    @Transactional(readOnly = true)
    public Page<PostResDto> getPostsByCategoryId(int categoryId, Pageable pageable) {
        return postRepository.findByCategoryId(categoryId, pageable)
                .map(PostResDto::fromEntity);
    }

    // // 페이징된 유저에 따른 게시글 조회
    // @Transactional(readOnly = true)
    // public Page<PostResDto> findAllPagedPostsByUser(Pageable pageable, int
    // userId) {
    // return postRepository.findAllByUserId(pageable, userId)
    // .map(PostResDto::fromEntity);
    // }

    // 제목 또는 내용으로 검색
    public Page<Post> searchPostsByTitleOrContent(String keyword, Pageable pageable) {
        return postRepository.findByTitleOrContent(keyword, pageable);
    }

    // 제목으로만 검색
    public Page<Post> searchPostsByTitle(String keyword, Pageable pageable) {
        return postRepository.findByTitle(keyword, pageable);
    }

    // 내용으로만 검색
    public Page<Post> searchPostsByContent(String keyword, Pageable pageable) {
        return postRepository.findByContent(keyword, pageable);
    }

    // 블로그관리 내 글 검색
    public Page<Post> searchPosts(Pageable pageable, String keyword, int type, int userId) {

        switch (type) {
            case 0: {
                return postRepository.findByTitleContainingAndUserId(keyword, userId, pageable);
            }
            case 1: {
                return postRepository.findByContentContainingAndUserId(keyword, userId, pageable);
            }
            case 2: {
                return postRepository.findByTitleContainingOrContentContainingAndUserId(keyword, userId,
                        pageable);
            }
            default:
                // 잘못된 타입이 전달된 경우 모든 게시글을 검색하여 반환
                return postRepository.findAllByUserId(pageable, userId);
        }
    }
}
