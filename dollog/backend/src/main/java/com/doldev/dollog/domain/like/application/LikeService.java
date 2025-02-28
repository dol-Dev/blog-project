package com.doldev.dollog.domain.like.application;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.comment.entity.Comment;
import com.doldev.dollog.domain.comment.repository.CommentRepository;
import com.doldev.dollog.domain.like.entity.Like;
import com.doldev.dollog.domain.like.repository.LikeRepository;
import com.doldev.dollog.domain.post.entity.Post;
import com.doldev.dollog.domain.post.repository.PostRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // 게시물 좋아요 토글
    @Transactional
    public void togglePostLike(int postId, CustomUserDetails userDetails) {
        processLikeInteraction(
                postId,
                userDetails,
                () -> findEntityById(postRepository::findById, postId, "Post not found"),
                likeRepository::findByPostIdAndUserId,
                Like::getPost,
                Post::incrementLikeCnt,
                Post::decrementLikeCnt,
                (builder, post) -> builder.post(post));
    }

    // 댓글 및 답글 좋아요 토글
    @Transactional
    public void toggleCommentLike(int commentId, CustomUserDetails userDetails) {
        processLikeInteraction(
                commentId,
                userDetails,
                () -> findEntityById(commentRepository::findById, commentId, "Comment not found"),
                likeRepository::findByCommentIdAndUserId,
                Like::getComment,
                Comment::incrementLikeCnt,
                Comment::decrementLikeCnt,
                (builder, comment) -> builder.comment(comment));
    }

    // 엔티티 ID로 엔티티 찾기
    private <T> T findEntityById(Function<Integer, Optional<T>> finder, int id, String errorMessage) {
        return finder.apply(id).orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }

    // 좋아요 상호작용 처리
    private <T> void processLikeInteraction(
            int entityId,
            CustomUserDetails userDetails,
            Supplier<T> entitySupplier,
            BiFunction<Integer, Integer, Optional<Like>> likeFinder,
            Function<Like, T> entityExtractor,
            Consumer<T> incrementAction,
            Consumer<T> decrementAction,
            BiConsumer<Like.LikeBuilder, T> entitySetter) {

        Optional<Like> likeOpt = Optional.empty();

        if (userDetails.isUser()) {
            likeOpt = likeFinder.apply(entityId, userDetails.getUser().getId());
        } else if (userDetails.isSnsUser()) {
            likeOpt = likeFinder.apply(entityId, userDetails.getSnsUser().getId());
        }

        T entity = entitySupplier.get();

        if (likeOpt.isEmpty()) {
            createNewLike(userDetails, entity, incrementAction, entitySetter);
        } else {
            toggleExistingLike(userDetails, likeOpt.get(), entity, incrementAction, decrementAction);
        }
    }

    // 새로운 좋아요 생성
    private <T> void createNewLike(CustomUserDetails userDetails, T entity, Consumer<T> incrementAction,
            BiConsumer<Like.LikeBuilder, T> entitySetter) {
                User user = userDetails.getUser();
                SnsUser snsUser = userDetails.getSnsUser();
        Like.LikeBuilder newLikeBuilder = Like.builder()
                .user(user)
                .snsUser(snsUser)
                .liked(true);
        entitySetter.accept(newLikeBuilder, entity);
        Like newLike = newLikeBuilder.build();
        incrementAction.accept(entity);
        likeRepository.save(newLike);
    }

    // 기존 좋아요 토글
    private <T> void toggleExistingLike(CustomUserDetails userDetails, Like like, T entity, Consumer<T> incrementAction,
            Consumer<T> decrementAction) {
        validateOwnership(like, userDetails);

        if (like.isLiked()) {
            decrementAction.accept(entity);
        } else {
            incrementAction.accept(entity);
        }
        like.toggleLiked();
    }

    // 좋아요 소유권 검증
    private void validateOwnership(Like like, CustomUserDetails userDetails) { 
        if (like.getUser().getId() != userDetails.getId()) {
            throw new IllegalArgumentException("Unauthorized operation");
        }
    }
}