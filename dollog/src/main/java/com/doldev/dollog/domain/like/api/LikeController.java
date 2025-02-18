package com.doldev.dollog.domain.like.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.like.application.LikeService;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController("/api/likes")
public class LikeController {

    private final LikeService likeService;

    // 게시물 좋아요 추가/취소
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<ApiResDto<Void>> togglePostLike(@PathVariable("postId") int postId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        likeService.togglePostLike(postId, userDetails.getUser());
        return ResponseEntity.ok()
                .body(ApiResDto.<Void>builder().messageCode("LIKE_TOGGLE_SUCCESS").build());
    }

    // 댓글 좋아요 추가/취소
    @PostMapping("/comments/{commentId}/like")
    public ResponseEntity<ApiResDto<Void>> toggleCommentLike(@PathVariable("commentId") int commentId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        likeService.toggleCommentLike(commentId, userDetails.getUser());
        return ResponseEntity.ok()
                .body(ApiResDto.<Void>builder().messageCode("LIKE_TOGGLE_SUCCESS").build());
    }
}