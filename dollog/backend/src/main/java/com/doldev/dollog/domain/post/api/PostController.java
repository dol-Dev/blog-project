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
import com.doldev.dollog.domain.post.entity.Post;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 게시글 생성
    @PostMapping("/")
    public ResponseEntity<ApiResDto<Void>> writePost(
            @RequestBody PostCreateReqDto postDto,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        postService.createPost(postDto, userDetails);

        return ResponseEntity
                .ok()
                .body(ApiResDto.<Void>builder()
                        .messageCode("게시글 작성 성공!")
                        .build());
    }

    // 게시글 삭제
    @DeleteMapping("/{postId}")
    public ResponseEntity<ApiResDto<Void>> deletePost(@PathVariable("postId") int postId) {
        postService.deletePost(postId);
        return ResponseEntity.ok().body(ApiResDto.<Void>builder().messageCode("게시글 삭제 성공!").build());
    }

    // 게시글 수정
    @PutMapping("/{postId}")
    public ResponseEntity<ApiResDto<Void>> updatePost(@PathVariable("postId") int postId,
            @RequestBody PostUpdateReqDto updatePostReqDto) {
        postService.updatePost(postId, updatePostReqDto);
        return ResponseEntity.ok().body(ApiResDto.<Void>builder().messageCode("게시글 수정 성공!").build());
    }

    // 게시글 검색
    @GetMapping("/search")
    public ResponseEntity<ApiResDto<Page<Post>>> searchPosts(
            @RequestParam("type") int type,
            @RequestParam("keyword") String keyword,
            @PageableDefault(size = 4, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<Post> posts;
        switch (type) {
            case 0: // 제목으로 검색
                posts = postService.searchPostsByTitle(keyword, pageable);
                break;
            case 1: // 내용으로 검색
                posts = postService.searchPostsByContent(keyword, pageable);
                break;
            case 2: // 제목 또는 내용으로 검색
                posts = postService.searchPostsByTitleOrContent(keyword, pageable);
                break;
            default:
                // 잘못된 검색 타입일 경우 빈 페이지 반환
                posts = Page.empty(pageable);
                return ResponseEntity.badRequest()
                        .body(ApiResDto.<Page<Post>>builder().messageCode("잘못된 검색 타입입니다.").data(posts).build());
        }

        return ResponseEntity.ok().body(ApiResDto.<Page<Post>>builder().messageCode("검색 성공!").data(posts).build());
    }

    // 모든 유저 게시글 조회
    @GetMapping("/")
    public ResponseEntity<ApiResDto<Page<Post>>> index(
            @PageableDefault(size = 4, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Post> posts = postService.findAllPagedPosts(pageable);
        return ResponseEntity
                .ok()
                .body(ApiResDto.<Page<Post>>builder()
                        .messageCode("게시글 조회 성공!")
                        .data(posts)
                        .build());
    }

    // 블로그별 게시글 조회
    @GetMapping("/{blogName}")
    public ResponseEntity<ApiResDto<Page<Post>>> getPostByBlogName(
            @PathVariable(name = "blogName") String blogName,
            @PageableDefault(size = 4, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

        // log.info("blogName : {}", blogName);
        Page<Post> posts = postService.findAllPagedPostsByBlogName(pageable, blogName);
        return ResponseEntity.ok()
                .body(ApiResDto.<Page<Post>>builder()
                        .messageCode("블로그 게시글 조회 성공!")
                        .data(posts)
                        .build());
    }

    // 상세 게시글 조회
    @GetMapping("/{postId}")
    public ResponseEntity<ApiResDto<Post>> getPostById(
            @PathVariable("postId") int postId) {
        Post post = postService.showPostDetail(postId);
        return ResponseEntity
        .ok()
        .body(ApiResDto.<Post>builder().messageCode("게시글 조회 성공!").data(post).build());
    }

    // 카테고리별 게시글 조회
    @GetMapping("/{categoryId}")
    public ResponseEntity<ApiResDto<Page<Post>>> getPostsByCategoryId(
            @PathVariable("categoryId") int categoryId,
            @PageableDefault(size = 4, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<Post> posts = postService.getPostsByCategoryId(categoryId, pageable);
        return ResponseEntity.ok()
                .body(ApiResDto.<Page<Post>>builder()
                        .messageCode("CATEGORY_GET_POSTS_SUCCESS")
                        .data(posts)
                        .build());
    }
}
