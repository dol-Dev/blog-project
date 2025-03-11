package com.doldev.dollog.domain.post.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.post.application.PostService;
import com.doldev.dollog.domain.post.dto.req.PostCreateReqDto;
import com.doldev.dollog.domain.post.dto.req.PostUpdateReqDto;
import com.doldev.dollog.domain.post.dto.res.PostResDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping
    public ResponseEntity<ApiResDto<Void>> createPost(
            @RequestBody PostCreateReqDto postDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        postService.createPost(postDto, userDetails);
        return ResponseEntity.ok(ApiResDto.<Void>builder().messageCode("POST_CREATE_SUCCESS").build());
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResDto<Void>> updatePost(
            @PathVariable("postId") int postId,
            @RequestBody PostUpdateReqDto updatePostReqDto) {

        postService.updatePost(postId, updatePostReqDto);
        return ResponseEntity.ok(ApiResDto.<Void>builder().messageCode("POST_UPDATE_SUCCESS").build());
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResDto<Void>> deletePost(@PathVariable("postId") int postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok(ApiResDto.<Void>builder().messageCode("POST_DELETE_SUCCESS").build());
    }

    // 특정 게시글 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResDto<PostResDto>> getPostByPostId(@PathVariable("postId") int postId) {
        return ResponseEntity.ok(ApiResDto.<PostResDto>builder()
                .messageCode("POST_GET_SUCCESS")
                .data(postService.getPostByPostId(postId))
                .build());
    }

    // 해당 유저의 게시글 조회(로그인 o)
    @GetMapping("/me")
    public ResponseEntity<ApiResDto<?>> getPostsByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResDto.<Page<PostResDto>>builder()
                .messageCode("POSTS_GET_SUCCESS")
                .data(postService.getPostsByUser(pageable, userDetails))
                .build());
    }

    // 페이징된 게시글 전체 조회 (로그인 유무X)
    @GetMapping
    public ResponseEntity<ApiResDto<Page<PostResDto>>> getAllPosts(
            @PageableDefault(size = 4, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResDto.<Page<PostResDto>>builder()
                .messageCode("POSTS_GET_SUCCESS")
                .data(postService.getAllPosts(pageable))
                .build());
    }

    // 닉네임에 따른 게시글 조회 (로그인 유무X)
    @GetMapping("/nickname")
    public ResponseEntity<ApiResDto<Page<PostResDto>>> getPostsByNickname(
            @RequestParam("nickname") String nickname,
            @RequestParam("provider") String provider,
            @PageableDefault(size = 4, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResDto.<Page<PostResDto>>builder()
                .messageCode("NICKNAME_POSTS_GET_SUCCESS")
                .data(postService.getPostsByNickname(pageable, nickname, provider))
                .build());
    }

    // 카테고리별 게시글 조회 (로그인 유무X)
    @GetMapping("/category")
    public ResponseEntity<ApiResDto<Page<PostResDto>>> getPostsByCategoryId(
            @RequestParam("categoryId") int categoryId,
            @PageableDefault(size = 4, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ApiResDto.<Page<PostResDto>>builder()
                .messageCode("CATEGORY_POSTS_GET_SUCCESS")
                .data(postService.getPostsByCategoryId(pageable, categoryId))
                .build());
    }

    // 게시글 검색 (제목, 내용, 제목+내용)
    @GetMapping("/search")
    public ResponseEntity<ApiResDto<Page<PostResDto>>> searchPosts(
            @RequestParam("type") int type,
            @RequestParam("keyword") String keyword,
            @PageableDefault(size = 4, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostResDto> posts = getSearchResults(type, keyword, pageable);
        return ResponseEntity.ok(ApiResDto.<Page<PostResDto>>builder()
                .messageCode("SEARCH_SUCCESS")
                .data(posts)
                .build());
    }

    private Page<PostResDto> getSearchResults(int type, String keyword, Pageable pageable) {
        return switch (type) {
            case 0 -> postService.searchPostsByTitle(keyword, pageable);
            case 1 -> postService.searchPostsByContent(keyword, pageable);
            case 2 -> postService.searchPostsByTitleOrContent(keyword, pageable);
            default -> Page.empty(pageable);
        };
    }

    // 블로그 관리 내 글 검색
    @GetMapping("/search/me")
    public ResponseEntity<ApiResDto<Page<PostResDto>>> searchUserPosts(
            @RequestParam("type") int type,
            @RequestParam("keyword") String keyword,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<PostResDto> posts = postService.searchPosts(pageable, keyword, type, userDetails);
        return ResponseEntity.ok(ApiResDto.<Page<PostResDto>>builder()
                .messageCode("USER_SEARCH_SUCCESS")
                .data(posts)
                .build());
    }
}
