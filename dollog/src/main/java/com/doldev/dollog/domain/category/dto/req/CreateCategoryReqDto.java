package com.doldev.dollog.domain.category.dto.req;

import java.util.List;

import lombok.Data;

@Data
public class CreateCategoryReqDto {
    private int id;
    private String name;
    private Integer  parentId;
    private List<Integer> childrenId;
    private Integer  postCount;
    private Integer  userId;
    private Integer parentOrderIndex; // 부모 순서 필드
    private Integer childOrderIndex; // 자식 순서 필드
}
