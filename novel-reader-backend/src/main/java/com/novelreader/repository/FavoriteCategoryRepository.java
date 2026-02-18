package com.novelreader.repository;

import com.novelreader.entity.FavoriteCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 收藏分类 Repository
 */
@Repository
public interface FavoriteCategoryRepository extends JpaRepository<FavoriteCategory, Long> {

    /**
     * 根据用户ID查询分类（按排序序号）
     */
    List<FavoriteCategory> findByUserIdOrderBySortOrderAsc(Long userId);
}
