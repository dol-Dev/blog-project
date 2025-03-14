package com.doldev.dollog.domain.comment.application;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.comment.dto.req.CommentCreateReqDto;
import com.doldev.dollog.domain.comment.dto.req.CommentUpdateReqDto;
import com.doldev.dollog.domain.comment.dto.res.CommentResDto;
import com.doldev.dollog.domain.comment.entity.Comment;
import com.doldev.dollog.domain.comment.repository.CommentRepository;
import com.doldev.dollog.domain.post.entity.Post;
import com.doldev.dollog.domain.post.repository.PostRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

        private final CommentRepository commentRepository;
        private final PostRepository postRepository;

        // 게시글에 대한 댓글 조회
        @Transactional(readOnly = true)
        public List<CommentResDto> getCommentsByPost(int postId) {
                return commentRepository.findByPostIdAndParentIsNull(postId)
                                .stream()
                                .map(CommentResDto::new)
                                .collect(Collectors.toList());
        }

        // 유저가 쓴 모든 댓글들 조회
        @Transactional
        public Page<CommentResDto> getCommentsByUser(Pageable pageable, CustomUserDetails userDetails) {
                Page<Comment> comments;
                if (userDetails.isUser()) {
                        comments = commentRepository.findByUserIdAndParentIsNull(userDetails.getId(), pageable);
                } else {
                        comments = commentRepository.findBySnsUserIdAndParentIsNull(userDetails.getId(), pageable);
                }
                return comments.map(CommentResDto::new);
        }

        // 블로그 관리 내 댓글 검색 (User & SnsUser)
        public Page<CommentResDto> getSearchPostsForBlog(Pageable pageable, String keyword, int type,
                        CustomUserDetails userDetails) {
                return switch (type) {
                        // type 0 : 내용 검색
                        case 0 ->
                                (userDetails.isSnsUser()
                                                ? commentRepository.findBySnsUserContentContaining(keyword, userDetails.getId(), pageable)
                                                : commentRepository.findByUserContentContaining(keyword, userDetails.getId(), pageable))
                                                .map(comment -> new CommentResDto(comment));
                        // type 1 : 사용자(닉네임) 검색
                        case 1 ->
                                (userDetails.isSnsUser()
                                                ? commentRepository.findBySnsUserNicknameContaining(keyword, userDetails.getId(), pageable)
                                                : commentRepository.findByUserNicknameContaining(keyword, userDetails.getId(), pageable))
                                                .map(comment -> new CommentResDto(comment));
                        // type 2 : 내용 + 사용자(닉네임) 검색
                        case 2 ->
                                (userDetails.isSnsUser()
                                                ? commentRepository.findBySnsUserContentOrNicknameContaining(keyword, userDetails.getId(), pageable)
                                                : commentRepository.findByUserContentOrNicknameContaining(keyword, userDetails.getId(), pageable))
                                                .map(comment -> new CommentResDto(comment));
                        default ->
                                (userDetails.isSnsUser()
                                                ? commentRepository.findAllBySnsUserId(pageable, userDetails.getId())
                                                : commentRepository.findAllByUserId(pageable, userDetails.getId()))
                                                .map(comment -> new CommentResDto(comment));
                };
        }

        // 댓글/답글 생성
        @Transactional
        public CommentResDto createComment(CommentCreateReqDto reqDto, CustomUserDetails userDetails) {
                // Post 조회
                Post post = postRepository.findById(reqDto.getPostId())
                                .orElseThrow(() -> new EntityNotFoundException("Post not found"));

                User user = null;
                SnsUser snsUser = null;

                // 둘중에 하나 존재하는거 꺼내기
                if (userDetails.isUser()) {
                        user = userDetails.getUser();
                } else if (userDetails.isSnsUser()) {
                        snsUser = userDetails.getSnsUser();
                        log.info("scuccess find snsUser : " + snsUser);
                }

                // 부모 댓글
                Comment parent = Optional.ofNullable(reqDto.getParentId())
                                .flatMap(commentRepository::findById)
                                .orElse(null);

                // 자식 댓글
                Comment comment = Comment.builder()
                                .content(reqDto.getContent())
                                .post(post)
                                .user(user)
                                .snsUser(snsUser)
                                .parent(parent)
                                .build();

                return new CommentResDto(commentRepository.save(comment));
        }

        // 댓글/답글 수정
        @Transactional
        public CommentResDto updateComment(int commentId, CommentUpdateReqDto reqDto) {
                Comment comment = commentRepository.findById(commentId)
                                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
                comment.updateContent(reqDto.getContent());
                return new CommentResDto(comment);
        }

        // 댓글/답글 삭제
        @Transactional
        public void deleteComment(int commentId) {
                commentRepository.deleteById(commentId);
        }
}