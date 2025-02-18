package com.doldev.dollog.domain.comment.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.comment.application.CommentService;
import com.doldev.dollog.domain.comment.dto.req.CommentCreateReqDto;
import com.doldev.dollog.domain.comment.dto.req.CommentUpdateReqDto;
import com.doldev.dollog.domain.comment.dto.res.CommentResDto;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 특정 게시글의 최상위 댓글 조회 (계층형 구조)
    @GetMapping("/post/{postId}")
    public ResponseEntity<ApiResDto<List<CommentResDto>>> getRootComments(@PathVariable int postId) {
        List<CommentResDto> comments = commentService.getRootCommentsByPost(postId);
        return ResponseEntity.ok()
                .body(ApiResDto.<List<CommentResDto>>builder()
                        .messageCode("COMMENT_LIST_SUCCESS")
                        .data(comments)
                        .build());
    }

    // 댓글/답글 생성
    @PostMapping
    public ResponseEntity<ApiResDto<CommentResDto>> createComment(@RequestBody CommentCreateReqDto request) {
        CommentResDto response = commentService.createComment(request);
        return ResponseEntity.ok()
                .body(ApiResDto.<CommentResDto>builder()
                        .messageCode("COMMENT_CREATE_SUCCESS")
                        .data(response)
                        .build());
    }

    // 댓글/답글 수정
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResDto<CommentResDto>> updateComment(
            @PathVariable int commentId,
            @RequestBody CommentUpdateReqDto request
    ) {
        CommentResDto response = commentService.updateComment(commentId, request);
        return ResponseEntity.ok()
                .body(ApiResDto.<CommentResDto>builder()
                        .messageCode("COMMENT_UPDATE_SUCCESS")
                        .data(response)
                        .build());
    }

    // 댓글/답글 삭제 (계층형 삭제)
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResDto<Void>> deleteComment(@PathVariable int commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.ok()
                .body(ApiResDto.<Void>builder()
                        .messageCode("COMMENT_DELETE_SUCCESS")
                        .build());
    }
}
