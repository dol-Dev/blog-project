package com.doldev.dollog.domain.category.api;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.doldev.dollog.domain.category.application.CategoryService;
import com.doldev.dollog.domain.category.dto.req.CategoryCreateReqDto;
import com.doldev.dollog.domain.category.dto.req.CategoryUpdateReqDto;
import com.doldev.dollog.domain.category.entity.Category;
import com.doldev.dollog.global.auth.principal.CustomUserDetails;
import com.doldev.dollog.global.dto.ApiResDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

        private final CategoryService categoryService;

        // 카테고리 생성
        @PostMapping
        public ResponseEntity<ApiResDto<Void>> createCategory(
                        @RequestBody CategoryCreateReqDto category,
                        @AuthenticationPrincipal CustomUserDetails userDetails) {
                categoryService.createCategory(category, userDetails);
                return ResponseEntity
                                .ok()
                                .body(ApiResDto.<Void>builder()
                                                .messageCode("CATEGORY_CREATE_SUCCESS")
                                                .build());
        }

        // 해당 유저의 페이징된 카테고리 조회
        @GetMapping
        public ResponseEntity<ApiResDto<Page<Category>>> getCategoriesByUserId(
                        @AuthenticationPrincipal CustomUserDetails userDetails,
                        @PageableDefault(size = 4) Pageable pageable) {
                Page<Category> categories = categoryService.getPagingCategories(userDetails, pageable);
                return ResponseEntity
                                .ok()
                                .body(ApiResDto.<Page<Category>>builder()
                                                .messageCode("CATEGORY_GET_PAGED_SUCCESS")
                                                .data(categories)
                                                .build());
        }

        // 해당 블로그의 모든 카테고리 조회(로그인 유무x)
        @GetMapping("/nickname")
        public ResponseEntity<ApiResDto<List<Category>>> getAllCategoriesByNickname(
                        @RequestParam("nickname") String nickname) {
                List<Category> categories = categoryService.getCategoriesByNickname(nickname);
                return ResponseEntity
                                .ok()
                                .body(ApiResDto.<List<Category>>builder()
                                                .messageCode("CATEGORY_GET_ALL_SUCCESS")
                                                .data(categories)
                                                .build());
        }

        // 카테고리 수정
        @PutMapping("/{categoryId}")
        public ResponseEntity<ApiResDto<Void>> updateCategory(@PathVariable("categoryId") int categoryId,
                        @RequestBody CategoryUpdateReqDto category) {
                categoryService.updateCategoryName(categoryId, category);
                return ResponseEntity
                                .ok()
                                .body(ApiResDto.<Void>builder()
                                                .messageCode("CATEGORY_UPDATE_SUCCESS")
                                                .build());
        }

        // 카테고리 삭제
        @DeleteMapping("/{categoryId}")
        public ResponseEntity<ApiResDto<Void>> deleteCategory(@PathVariable("categoryId") int categoryId) {
                categoryService.deleteCategory(categoryId);
                return ResponseEntity
                                .ok()
                                .body(ApiResDto.<Void>builder()
                                                .messageCode("CATEGORY_DELETE_SUCCESS")
                                                .build());
        }

        // 카테고리 순서 변경
        @PutMapping("/order")
        public ResponseEntity<ApiResDto<Void>> updateCategoryOrder(@RequestBody List<Integer> categoryIds) {
                categoryService.updateCategoryOrder(categoryIds);
                return ResponseEntity
                                .ok()
                                .body(ApiResDto.<Void>builder()
                                                .messageCode("CATEGORY_ORDER_UPDATE_SUCCESS")
                                                .build());
        }
}