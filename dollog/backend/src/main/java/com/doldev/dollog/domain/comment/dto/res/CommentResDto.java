package com.doldev.dollog.domain.comment.dto.res;

import java.time.LocalDateTime;
import java.util.List;

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
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
        
        if (comment.getUser() != null) {
            this.writer = new WriterInfo(comment.getUser());
        } else if (comment.getSnsUser() != null) {
            this.writer = new WriterInfo(comment.getSnsUser());
        } 

        this.createdAt = comment.getCreateDate();
        this.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        this.replies = comment.getChild().stream()
                .map(CommentResDto::new)
                .toList();
    }

    @Getter
    private class WriterInfo {
        private final int userId;
        private final String nickname;
        private final String avatarImageName;

        // User 
        public WriterInfo(User user) {
            this.userId = user.getId();
            this.nickname = user.getProfile().getNickname();
            this.avatarImageName = user.getProfile().getAvatarImageName();
        }

        // SnsUser
        public WriterInfo(SnsUser snsUser) {
            this.userId = snsUser.getId();  
            this.nickname = snsUser.getProfile().getNickname();
            this.avatarImageName = snsUser.getProfile().getAvatarImageName();
        }
    }
}