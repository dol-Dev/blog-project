package com.doldev.dollog.domain.post.dto.req;

import lombok.Data;

@Data
public class CreatePostReqDto {
    private String content;
    private String title;
    private int categoryId;
}
