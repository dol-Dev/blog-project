package com.doldev.dollog.domain.comment.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CommentUpdateReqDto {
    @NotBlank
    private String content;
}
