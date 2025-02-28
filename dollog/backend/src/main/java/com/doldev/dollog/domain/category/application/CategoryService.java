package com.doldev.dollog.domain.category.application;

import java.util.ArrayList;
import java.util.Collections;
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

import com.doldev.dollog.domain.account.snsUser.entity.SnsUser;
import com.doldev.dollog.domain.account.snsUser.repository.SnsUserRepository;
import com.doldev.dollog.domain.account.user.entity.User;
import com.doldev.dollog.domain.account.user.repository.UserRepository;
import com.doldev.dollog.domain.category.dto.req.CategoryCreateReqDto;
import com.doldev.dollog.domain.category.dto.req.CategoryUpdateReqDto;
import com.doldev.dollog.domain.category.entity.Category;
import com.doldev.dollog.domain.category.repository.CategoryRepository;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final SnsUserRepository snsUserRepository;

    // 카테고리 생성
    @Transactional
    public Category createCategory(CategoryCreateReqDto reqDto, CustomUserDetails userDetails) {

        // user 혹은 snsUser 가져오기
        User user = userDetails.getUser();
        SnsUser snsUser = userDetails.getSnsUser();

        // 빌더 초기화
        Category.CategoryBuilder categoryBuilder = Category.builder()
                .name(reqDto.getName());

        // DB 유효성 검사
        if (user != null) {
            userRepository.findById(user.getId())
                    .orElseThrow(() -> new RuntimeException("해당 User가 존재하지 않습니다. ID:" + user.getId()));
            categoryBuilder.user(user);
        } else {
            snsUserRepository.findById(snsUser.getId())
                    .orElseThrow(() -> new RuntimeException("해당 SNS User가 존재하지 않습니다. ID:" + snsUser.getId()));
            categoryBuilder.snsUser(snsUser);
        }

        // 부모/자식 분기 처리
        if (reqDto.getParentId() == null) {
            // 부모 카테고리 생성
            categoryBuilder.parent(null);

            int maxParentOrder;
            if (user != null) {
                maxParentOrder = categoryRepository.findMaxParentOrderIndexByUserId(user.getId());
            } else {
                maxParentOrder = categoryRepository.findMaxParentOrderIndexBySnsUserId(snsUser.getId());
            }
            categoryBuilder.parentOrderIndex(maxParentOrder + 1);

        } else {
            // 자식 카테고리 생성
            Category parent = categoryRepository.findById(reqDto.getParentId())
                    .orElseThrow(() -> new IllegalArgumentException(
                            "부모 카테고리가 존재하지 않습니다. ID: " + reqDto.getParentId()));
            categoryBuilder.parent(parent);

            int maxChildOrder = categoryRepository.findMaxChildOrderIndexByParentId(parent.getId());
            categoryBuilder.childOrderIndex(maxChildOrder + 1);
        }

        // 빌더로 카테고리 객체 생성 후 저장
        Category category = categoryBuilder.build();
        return categoryRepository.save(category);
    }

    // 부모/자식 카테고리 정렬 업데이트 
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
                category.changeParentOrderIndex(i);
            } else {
                category.changeChildOrderIndex(i);
            }
        }

        categoryRepository.saveAll(categories);
    }

    // 카테고리 이름 수정
    @Transactional
    public Category updateCategoryName(int categoryId, CategoryUpdateReqDto reqDto) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다. ID: " + categoryId));

        if (StringUtils.isNotBlank(reqDto.getName())) {
            category.changeName(reqDto.getName());
        }

        return categoryRepository.save(category);
    }

    // 카테고리 삭제 
    @Transactional
    public void deleteCategory(int categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("해당 카테고리가 존재하지 않습니다. ID: " + categoryId));
        categoryRepository.delete(category);
    }

    // 카테고리 단건 조회 
    public Category findCategoryById(int categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "카테고리 ID: " + categoryId + "에 해당하는 카테고리가 없습니다."));
    }

    // 페이징된 카테고리 조회
    @Transactional
    public Page<Category> getPagingCategories(CustomUserDetails userDetails, Pageable pageable) {
        User user = userDetails.getUser();
        SnsUser snsUser = userDetails.getSnsUser();

        if (user == null && snsUser == null) {
            // 사용자 정보가 전혀 없는 경우
            return Page.empty(pageable);
        }

        Page<Category> parentPage;
        if (user != null) {
            parentPage = categoryRepository.findParentCategoriesByUserId(user.getId(), pageable);
        } else {
            parentPage = categoryRepository.findParentCategoriesBySnsUserId(snsUser.getId(), pageable);
        }

        // 부모 ID 목록 추출
        List<Integer> parentIds = parentPage.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        // 자식 카테고리 조회
        Map<Integer, List<Category>> childrenMap = categoryRepository
                .findChildCategoriesByParentIds(parentIds)
                .stream()
                .collect(Collectors.groupingBy(child -> child.getParent().getId()));

        // 부모에 자식들 매핑
        parentPage.forEach(parent -> {
            List<Category> children = childrenMap.getOrDefault(parent.getId(), new ArrayList<>());
            children.forEach(parent::addChild);
        });

        return new PageImpl<>(
                parentPage.getContent(),
                pageable,
                parentPage.getTotalElements());
    }

    // 카테고리 조회(로그인 유무x)
    @Transactional
    public List<Category> getCategoriesByUserId(int userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        Optional<SnsUser> snsUserOpt = snsUserRepository.findById(userId);

        if (userOpt.isPresent()) {
            // 일반 유저
            return categoryRepository.findAllParentsWithChildrenByUserId(userId);
        } else if (snsUserOpt.isPresent()) {
            // SNS 유저
            return categoryRepository.findAllParentsWithChildrenBySnsUserId(userId);
        } else {
            // 둘 다 없으면 빈 리스트
            return Collections.emptyList();
        }
    }

    // 내부 메서드
    public Optional<Category> getCategoryById(int categoryId) {
        return categoryRepository.findById(categoryId);
    }

}
