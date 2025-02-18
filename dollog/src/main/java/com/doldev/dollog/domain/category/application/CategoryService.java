package com.doldev.dollog.domain.category.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.domain.category.dto.req.CreateCategoryReqDto;
import com.doldev.dollog.domain.category.dto.req.UpdateCategoryReqDto;
import com.doldev.dollog.domain.category.entity.Category;
import com.doldev.dollog.domain.category.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public Category createCategory(CreateCategoryReqDto reqDto) {
        Category category = new Category();
        category.setName(reqDto.getName());

        User user = userRepository.findById(reqDto.getUserId())
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
        category.setUser(user);

        if (reqDto.getParentId() == null) {
            // 부모 카테고리 생성
            category.setParent(null);
            int maxParentOrder = categoryRepository.findMaxParentOrderIndexByUserId(user.getId());
            category.setParentOrderIndex(maxParentOrder + 1);
        } else {
            // 자식 카테고리 생성
            Category parent = categoryRepository.findById(reqDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "부모 카테고리가 존재하지 않습니다. ID: " + reqDto.getParentId()));
            category.setParent(parent);
            int maxChildOrder = categoryRepository.findMaxChildOrderIndexByParentId(parent.getId());
            category.setChildOrderIndex(maxChildOrder + 1);
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void updateCategoryOrder(List<Integer> categoryIds) {
        List<Category> categories = categoryRepository.findAllById(categoryIds);

        // 모든 카테고리가 부모인지 자식인지 확인
        boolean isParent = categories.get(0).getParent() == null;
        boolean isAllSameType = categories.stream()
                .allMatch(c -> (c.getParent() == null) == isParent);

        if (!isAllSameType) {
            throw new IllegalArgumentException("부모와 자식 카테고리를 함께 정렬할 수 없습니다.");
        }

        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if (isParent) {
                category.setParentOrderIndex(i);
            } else {
                category.setChildOrderIndex(i);
            }
        }

        categoryRepository.saveAll(categories);
    }

    @Transactional
    public Category updateCategoryName(int categoryId, UpdateCategoryReqDto reqDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다. ID: " + categoryId));

        // 이름 업데이트
        if (StringUtils.isNotBlank(reqDto.getName())) {
            category.setName(reqDto.getName());
        }

        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(int categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다. ID: " + categoryId));
        categoryRepository.delete(category);
    }

    public Category findCategoryById(int categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "카테고리 ID: " + categoryId + "에 해당하는 카테고리가 없습니다."));
    }

    @Transactional
    public Page<Category> getPagingCategoriesByUserId(int userId, Pageable pageable) {
        // 1. 부모 카테고리 페이징 조회
        Page<Category> parentPage = categoryRepository.findParentCategoriesByUserId(
                userId, pageable);

        // 2. 부모 ID 목록 추출
        List<Integer> parentIds = parentPage.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        // 3. 자식 카테고리 조회 및 매핑
        Map<Integer, List<Category>> childrenMap = categoryRepository
                .findChildCategoriesByParentIds(parentIds)
                .stream()
                .collect(Collectors.groupingBy(child -> child.getParent().getId()));

        parentPage.forEach(parent -> parent.setChildren(childrenMap.getOrDefault(parent.getId(), new ArrayList<>())));

        return new PageImpl<>(
                parentPage.getContent(),
                pageable,
                parentPage.getTotalElements());
    }

    @Transactional
    public List<Category> getCategoriesByUserId(int userId) {
        // 1. 계층 구조 전체 조회 (부모 + 자식)
        return categoryRepository.findAllParentsWithChildrenByUserId(userId);
    }

    public Optional<Category> getCategoryById(int categoryId) {
        return categoryRepository.findById(categoryId);
    }

}
