package com.doldev.dollog.domain.post.application;

import org.apache.commons.lang3.StringUtils;
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
    public void createPost(PostCreateReqDto reqDto, CustomUserDetails userDetails) {
        Category category = categoryService.findCategoryById(reqDto.getCategoryId());

        Post post = Post.builder()
                .title(reqDto.getTitle())
                .content(reqDto.getContent())
                .category(category)
                .user(userDetails.getUser())
                .snsUser(userDetails.getSnsUser())
                .count(0) // 조회수 초기화
                .build();
        postRepository.save(post);
    }

    // 게시글 수정
    @Transactional
    public void updatePost(int postId, PostUpdateReqDto reqDto) {
        Category category = categoryService.findCategoryById(reqDto.getCategoryId());

        postRepository.findById(postId)
                .ifPresentOrElse(post -> {
                    post.updateTitle(reqDto.getTitle());
                    post.updateContent(reqDto.getContent());
                    post.assignCategory(category);
                }, () -> {
                    throw new IllegalArgumentException("게시글 찾기 실패 ID: " + postId);
                });
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
        return postRepository.findAll(pageable).map(PostResDto::fromEntity);
    }

    // 닉네임으로 게시글 조회 (User & SnsUser)
    @Transactional(readOnly = true)
    public Page<PostResDto> getPostsByNickname(Pageable pageable, String nickname, String provider) {
        return (StringUtils.isNotBlank(provider) ? postRepository.findAllBySnsUserNickname(pageable, nickname)
                : postRepository.findAllByUserNickname(pageable, nickname))
                .map(PostResDto::fromEntity);
    }

    // 카테고리별 게시글 조회
    @Transactional(readOnly = true)
    public Page<PostResDto> getPostsByCategoryId(Pageable pageable, int categoryId) {
        return postRepository.findByCategoryId(categoryId, pageable).map(PostResDto::fromEntity);
    }

    // 제목 또는 내용으로 검색
    public Page<PostResDto> searchPostsByTitleOrContent(String keyword, Pageable pageable) {
        return postRepository.findByTitleOrContent(keyword, pageable).map(PostResDto::fromEntity);
    }

    // 제목으로만 검색
    public Page<PostResDto> searchPostsByTitle(String keyword, Pageable pageable) {
        return postRepository.findByTitle(keyword, pageable).map(PostResDto::fromEntity);
    }

    // 내용으로만 검색
    public Page<PostResDto> searchPostsByContent(String keyword, Pageable pageable) {
        return postRepository.findByContent(keyword, pageable).map(PostResDto::fromEntity);
    }

    // 블로그 관리 내 글 검색 (User & SnsUser)
    public Page<PostResDto> searchPosts(Pageable pageable, String keyword, int type, int userId, String provider) {
        return switch (type) {
            case 0 ->
                (StringUtils.isNotBlank(provider)
                        ? postRepository.findBySnsUserTitleContaining(keyword, userId, pageable)
                        : postRepository.findByUserTitleContaining(keyword, userId, pageable))
                        .map(PostResDto::fromEntity);
            case 1 ->
                (StringUtils.isNotBlank(provider)
                        ? postRepository.findBySnsUserContentContaining(keyword, userId, pageable)
                        : postRepository.findByUserContentContaining(keyword, userId, pageable))
                        .map(PostResDto::fromEntity);
            case 2 -> (StringUtils.isNotBlank(provider)
                    ? postRepository.findBySnsUserTitleOrContentContaining(keyword, userId, pageable)
                    : postRepository.findByUserTitleOrContentContaining(keyword, userId, pageable))
                    .map(PostResDto::fromEntity);
            default -> (StringUtils.isNotBlank(provider) ? postRepository.findAllBySnsUserId(pageable, userId)
                    : postRepository.findAllByUserId(pageable, userId))
                    .map(PostResDto::fromEntity);
        };
    }
}
