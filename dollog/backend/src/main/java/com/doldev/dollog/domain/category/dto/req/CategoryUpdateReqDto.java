package com.doldev.dollog.domain.category.dto.req;

import java.util.List;

import lombok.Getter;

@Getter
public class CategoryUpdateReqDto {
    private int id;
    private String name;
    private int parentId;
    private List<Integer> childrenId;
    private int postCount;
    private int userId;
    private int parentOrderIndex;
    private int childOrderIndex;
}
