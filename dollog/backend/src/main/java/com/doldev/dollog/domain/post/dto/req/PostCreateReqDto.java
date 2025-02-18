package com.doldev.dollog.domain.post.dto.req;

import lombok.Getter;

@Getter
public class PostCreateReqDto {
    private String content;
    private String title;
    private int categoryId;
}
