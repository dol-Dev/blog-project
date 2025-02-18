package com.doldev.dollog.domain.category.dto.req;

import java.util.List;

import lombok.Getter;

@Getter
public class CategoryCreateReqDto {
    private int id;
    private String name;
    private Integer parentId;
    private List<Integer> childrenId;
    private int postCount;
    private int userId;
    private int parentOrderIndex; // 부모 순서 필드O
    private int childOrderIndex; // 자식 순서 필드
}
