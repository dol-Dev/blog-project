package com.doldev.dollog.domain.like.application;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.comment.entity.Comment;
import com.doldev.dollog.domain.comment.repository.CommentRepository;
import com.doldev.dollog.domain.like.entity.Like;
import com.doldev.dollog.domain.like.repository.LikeRepository;
import com.doldev.dollog.domain.post.entity.Post;
import com.doldev.dollog.domain.post.repository.PostRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public void togglePostLike(int postId, User user) {
        processLikeInteraction(
                postId,
                user,
                () -> findEntityById(postRepository::findById, postId, "Post not found"),
                likeRepository::findByPostIdAndUserId,
                Like::getPost,
                Post::incrementLikeCnt,
                Post::decrementLikeCnt,
                (builder, post) -> builder.post(post));
    }

    @Transactional
    public void toggleCommentLike(int commentId, User user) {
        processLikeInteraction(
                commentId,
                user,
                () -> findEntityById(commentRepository::findById, commentId, "Comment not found"),
                likeRepository::findByCommentIdAndUserId,
                Like::getComment,
                Comment::incrementLikeCnt,
                Comment::decrementLikeCnt,
                (builder, comment) -> builder.comment(comment));
    }

    private <T> T findEntityById(Function<Integer, Optional<T>> finder, int id, String errorMessage) {
        return finder.apply(id).orElseThrow(() -> new IllegalArgumentException(errorMessage));
    }

    private <T> void processLikeInteraction(
            int entityId,
            User user,
            Supplier<T> entitySupplier,
            BiFunction<Integer, Integer, Optional<Like>> likeFinder,
            Function<Like, T> entityExtractor,
            Consumer<T> incrementAction,
            Consumer<T> decrementAction,
            BiConsumer<Like.LikeBuilder, T> entitySetter) {
        Optional<Like> existingLike = likeFinder.apply(entityId, user.getId());
        T entity = entitySupplier.get();

        if (existingLike.isEmpty()) {
            createNewLike(user, entity, incrementAction, entitySetter);
        } else {
            toggleExistingLike(user, existingLike.get(), entity, incrementAction, decrementAction);
        }
    }

    private <T> void createNewLike(User user, T entity, Consumer<T> incrementAction,
            BiConsumer<Like.LikeBuilder, T> entitySetter) {
        Like.LikeBuilder newLikeBuilder = Like.builder()
                .user(user)
                .liked(true);
        entitySetter.accept(newLikeBuilder, entity);
        Like newLike = newLikeBuilder.build();
        incrementAction.accept(entity);
        likeRepository.save(newLike);
    }

    private <T> void toggleExistingLike(User user, Like like, T entity, Consumer<T> incrementAction,
            Consumer<T> decrementAction) {
        validateOwnership(like, user);

        if (like.isLiked()) {
            decrementAction.accept(entity);
        } else {
            incrementAction.accept(entity);
        }
        like.toggleLiked();
    }

    private void validateOwnership(Like like, User user) {
        if (like.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("Unauthorized operation");
        }
    }
}