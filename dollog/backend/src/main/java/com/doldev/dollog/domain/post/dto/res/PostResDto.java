package com.doldev.dollog.domain.post.dto.res;

import java.time.LocalDateTime;

import com.doldev.dollog.domain.category.entity.Category;
import com.doldev.dollog.domain.post.entity.Post;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PostResDto {
    private int id;
    private String title;
    private String content;
    private String nickname;
    private LocalDateTime createDate;
    private Category category;
    private int likeCnt;
    private String avatarImageName;

    public static PostResDto fromEntity(Post post) {
        return PostResDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .likeCnt(post.getLikeCnt())
                .createDate(post.getCreateDate())
                .nickname(post.getUser().getProfile().getNickname())
                .category(post.getCategory() != null ? post.getCategory() : null)
                .avatarImageName(post.getUser().getProfile().getAvatarImageName())
                .build();
    }
}
