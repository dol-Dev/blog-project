package com.doldev.dollog.domain.category.dto.res;

import com.doldev.dollog.domain.category.entity.Category;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResDto {
    private int id;
    private String name;

    public static CategoryResDto fromEntity(Category category) {
        if (category == null)
            return null;
        return CategoryResDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
