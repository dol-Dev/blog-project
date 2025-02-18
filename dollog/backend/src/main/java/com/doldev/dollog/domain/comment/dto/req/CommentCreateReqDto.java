package com.doldev.dollog.domain.comment.dto.req;

import lombok.Getter;

@Getter
public class CommentCreateReqDto {
    private String content;
    private int parentId; // 답글인 경우 부모 ID
    private int postId;
    private int userId;
}