package com.doldev.dollog.domain.category.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.doldev.dollog.domain.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

        // == User ==
        List<Category> findByUserId(int userId);

        // 부모+자식 전체 계층 구조 조회
        @Query("SELECT DISTINCT p FROM Category p "
                        + "LEFT JOIN FETCH p.children c "
                        + "WHERE p.user.id = :userId AND p.parent IS NULL "
                        + "ORDER BY p.parentOrderIndex ASC, c.childOrderIndex ASC")
        List<Category> findAllParentsWithChildrenByUserId(@Param("userId") Integer userId);

        // 부모 카테고리만 페이징 조회
        @Query("SELECT c FROM Category c "
                        + "WHERE c.user.id = :userId AND c.parent IS NULL "
                        + "ORDER BY c.parentOrderIndex ASC")
        Page<Category> findParentCategoriesByUserId(
                        @Param("userId") Integer userId,
                        Pageable pageable);

        // 부모 카테고리의 최대 parentOrderIndex 조회
        @Query("SELECT COALESCE(MAX(c.parentOrderIndex), 0) "
                        + "FROM Category c "
                        + "WHERE c.user.id = :userId AND c.parent IS NULL")
        int findMaxParentOrderIndexByUserId(@Param("userId") Integer userId);


        // == SnsUser ==
        List<Category> findBySnsUserId(int snsUserId);

        // 부모+자식 전체 계층 구조 조회(SnsUser)
        @Query("SELECT DISTINCT p FROM Category p "
                        + "LEFT JOIN FETCH p.children c "
                        + "WHERE p.snsUser.id = :snsUserId AND p.parent IS NULL "
                        + "ORDER BY p.parentOrderIndex ASC, c.childOrderIndex ASC")
        List<Category> findAllParentsWithChildrenBySnsUserId(@Param("snsUserId") Integer snsUserId);

        // 부모 카테고리만 페이징 조회(SnsUser)
        @Query("SELECT c FROM Category c "
                        + "WHERE c.snsUser.id = :snsUserId AND c.parent IS NULL "
                        + "ORDER BY c.parentOrderIndex ASC")
        Page<Category> findParentCategoriesBySnsUserId(
                        @Param("snsUserId") Integer snsUserId,
                        Pageable pageable);

        // 부모 카테고리의 최대 parentOrderIndex 조회(SnsUser)
        @Query("SELECT COALESCE(MAX(c.parentOrderIndex), 0) "
                        + "FROM Category c "
                        + "WHERE c.snsUser.id = :snsUserId AND c.parent IS NULL")
        int findMaxParentOrderIndexBySnsUserId(@Param("snsUserId") Integer snsUserId);


        // == 자식 카테고리 관련 == 
        // 자식 카테고리 조회 (childOrderIndex 기준)
        @Query("SELECT c FROM Category c "
                        + "WHERE c.parent.id IN :parentIds "
                        + "ORDER BY c.childOrderIndex ASC")
        List<Category> findChildCategoriesByParentIds(
                        @Param("parentIds") List<Integer> parentIds);

        // 자식 카테고리의 최대 childOrderIndex 조회
        @Query("SELECT COALESCE(MAX(c.childOrderIndex), 0) "
                        + "FROM Category c "
                        + "WHERE c.parent.id = :parentId")
        int findMaxChildOrderIndexByParentId(@Param("parentId") Integer parentId);
}
