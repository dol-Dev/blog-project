package com.doldev.dollog.domain.comment.application;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.domain.comment.dto.req.CommentCreateReqDto;
import com.doldev.dollog.domain.comment.dto.req.CommentUpdateReqDto;
import com.doldev.dollog.domain.comment.dto.res.CommentResDto;
import com.doldev.dollog.domain.comment.entity.Comment;
import com.doldev.dollog.domain.comment.repository.CommentRepository;
import com.doldev.dollog.domain.post.entity.Post;
import com.doldev.dollog.domain.post.repository.PostRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 게시글에 대한 최상위 댓글 조회
    @Transactional(readOnly = true)
    public List<CommentResDto> getRootCommentsByPost(int postId) {
        return commentRepository.findByPostIdAndParentIsNull(postId).stream()
                .map(CommentResDto::new)
                .collect(Collectors.toList());
    }

    // 댓글/답글 생성
    @Transactional
    public CommentResDto createComment(CommentCreateReqDto reqDto) {
        Post post = postRepository.findById(reqDto.getPostId())
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        User user = userRepository.findById(reqDto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Comment parent = Optional.ofNullable(reqDto.getParentId())
                .flatMap(commentRepository::findById)
                .orElse(null);

        Comment comment = Comment.builder()
                .content(reqDto.getContent())
                .post(post)
                .user(user)
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
        commentRepository.deleteById(commentId); // 댓글 삭제 시 대댓글도 cascade로 삭제 가능
    }
}