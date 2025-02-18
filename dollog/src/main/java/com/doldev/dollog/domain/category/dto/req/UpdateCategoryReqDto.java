package com.doldev.dollog.domain.category.dto.req;

import java.util.List;

import lombok.Data;

@Data
public class UpdateCategoryReqDto {
    private int id;
    private String name;
    private Integer  parentId;
    private List<Integer> childrenId;
    private Integer  postCount;
    private Integer  userId;
    private Integer parentOrderIndex;
    private Integer childOrderIndex; 
}

