package com.doldev.dollog.domain.comment.api;

import java.util.List;

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

import com.doldev.dollog.domain.comment.application.CommentService;
import com.doldev.dollog.domain.comment.dto.req.CommentCreateReqDto;
import com.doldev.dollog.domain.comment.dto.req.CommentUpdateReqDto;
import com.doldev.dollog.domain.comment.dto.res.CommentResDto;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

        private final CommentService commentService;

        // 특정 게시글의 최상위 댓글 조회 (계층형 구조)
        @GetMapping("/post/{postId}")
        public ResponseEntity<ApiResDto<List<CommentResDto>>> getCommentsByPost(@PathVariable int postId) {
                List<CommentResDto> comments = commentService.getCommentsByPost(postId);
                return ResponseEntity.ok()
                                .body(ApiResDto.<List<CommentResDto>>builder()
                                                .messageCode("COMMENT_LIST_SUCCESS")
                                                .data(comments)
                                                .build());
        }

        // 댓글 불러오기
        @GetMapping("/me")
        public ResponseEntity<ApiResDto<?>> getCommentsByUser(@AuthenticationPrincipal CustomUserDetails userDetails,
                        @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
                Page<CommentResDto> comments = commentService.getCommentsByUser(pageable, userDetails);
                return ResponseEntity.ok().body(ApiResDto.builder()
                                .messageCode("댓글 조회 성공!")
                                .data(comments)
                                .build());
        }

        // 댓글/답글 생성
        @PostMapping
        public ResponseEntity<ApiResDto<CommentResDto>> createComment(
                        @RequestBody CommentCreateReqDto reqDto,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                CommentResDto response = commentService.createComment(reqDto, userDetails);
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
                        @RequestBody CommentUpdateReqDto reqDto) {
                CommentResDto response = commentService.updateComment(commentId, reqDto);
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

        // 블로그 관리 내 댓글 검색
        @GetMapping("/search/me")
        public ResponseEntity<ApiResDto<Page<CommentResDto>>> getSearchPostsForBlog(
                        @RequestParam("type") int type,
                        @RequestParam("keyword") String keyword,
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @PageableDefault(size = 12, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {

                Page<CommentResDto> comments = commentService.getSearchPostsForBlog(pageable, keyword, type, userDetails);
                return ResponseEntity.ok(ApiResDto.<Page<CommentResDto>>builder()
                                .messageCode("USER_COMMENT_SEARCH_SUCCESS")
                                .data(comments)
                                .build());
        }
}
