package com.doldev.dollog.domain.comment.dto.res;

import java.time.LocalDateTime;
import java.util.List;

import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.comment.entity.Comment;

import lombok.Getter;

@Getter
public class CommentResDto {
    private int id;
    private String content;
    private WriterInfo writer;
    private LocalDateTime createdAt;
    private Integer parentId; 
    private List<CommentResDto> replies; // 자식 댓글

    public CommentResDto(Comment comment) {
        this.id = comment.getId();
        this.content = comment.getContent();
        this.writer = new WriterInfo(comment.getUser());
        this.createdAt = comment.getCreateDate();
        this.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        this.replies = comment.getChild().stream()
                .map(CommentResDto::new)
                .toList();
    }

    @Getter
    private static class WriterInfo {
        private final int userId;
        private final String nickname;
        private final String avatarImageName;

        public WriterInfo(User user) {
            this.userId = user.getId();
            this.nickname = user.getProfile().getNickname();
            this.avatarImageName = user.getProfile().getAvatarImageName();
        }
    }
}
