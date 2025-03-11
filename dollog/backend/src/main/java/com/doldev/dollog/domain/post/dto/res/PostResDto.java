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
    private String provider;
    private String blogName;
    private LocalDateTime createDate;
    private Category category;
    private int likeCnt;
    private String avatarImageName;

    public static PostResDto fromEntity(Post post) {
        String nickname = null;
        String avatarImageName = null;
        String provider = null;
        String blogName = null;

        if (post.getUser() != null) {
            nickname = post.getUser().getProfile().getNickname();
            avatarImageName = post.getUser().getProfile().getAvatarImageName();
            blogName = post.getUser().getProfile().getBlogName();
        } else if (post.getSnsUser() != null) {
            nickname = post.getSnsUser().getProfile().getNickname();
            avatarImageName = post.getSnsUser().getProfile().getAvatarImageName();
            blogName = post.getSnsUser().getProfile().getBlogName();
            provider = post.getSnsUser().getProvider().name();
        }

        return PostResDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .likeCnt(post.getLikeCnt())
                .createDate(post.getCreateDate())
                .nickname(nickname)
                .provider(provider)
                .blogName(blogName)
                .avatarImageName(avatarImageName)
                .category(post.getCategory()) 
                .build();
    }
}
