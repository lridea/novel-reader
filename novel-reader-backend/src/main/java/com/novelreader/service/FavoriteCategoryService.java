package com.novelreader.service;

import com.novelreader.entity.Favorite;
import com.novelreader.entity.FavoriteCategory;
import com.novelreader.repository.FavoriteCategoryRepository;
import com.novelreader.repository.FavoriteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class FavoriteCategoryService {

    @Autowired
    private FavoriteCategoryRepository favoriteCategoryRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Transactional
    public Map<String, Object> createCategory(Long userId, String name, String description) {
        log.info("创建分类: userId={}, name={}", userId, name);

        Map<String, Object> result = new HashMap<>();

        // 检查名称长度
        if (name == null || name.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "收藏夹名称不能为空");
            return result;
        }
        if (name.length() > 9) {
            result.put("success", false);
            result.put("message", "收藏夹名称不能超过9个字符");
            return result;
        }
        if (description != null && description.length() > 200) {
            result.put("success", false);
            result.put("message", "收藏夹描述不能超过200个字符");
            return result;
        }

        // 检查是否已存在同名收藏夹
        if (favoriteCategoryRepository.existsByUserIdAndName(userId, name)) {
            result.put("success", false);
            result.put("message", "收藏夹名称已存在");
            return result;
        }

        FavoriteCategory category = new FavoriteCategory();
        category.setUserId(userId);
        category.setName(name);
        category.setDescription(description);
        category.setIsDefault(false);
        category.setSortOrder(0);

        FavoriteCategory savedCategory = favoriteCategoryRepository.save(category);

        result.put("success", true);
        result.put("message", "创建成功");
        result.put("category", toCategoryInfo(savedCategory, 0L));

        return result;
    }

    public Map<String, Object> getCategoryList(Long userId) {
        log.info("获取分类列表: userId={}", userId);

        Map<String, Object> result = new HashMap<>();

        List<FavoriteCategory> categories = favoriteCategoryRepository.findByUserIdOrderBySortOrderAsc(userId);

        result.put("success", true);
        result.put("categories", categories.stream().map(c -> {
            long count = favoriteRepository.countByUserIdAndCategoryId(userId, c.getId());
            return toCategoryInfo(c, count);
        }).toList());

        return result;
    }

    @Transactional
    public Map<String, Object> updateCategory(Long userId, Long categoryId, String name, String description, Integer sortOrder) {
        log.info("更新分类: userId={}, categoryId={}", userId, categoryId);

        Map<String, Object> result = new HashMap<>();

        Optional<FavoriteCategory> categoryOpt = favoriteCategoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "分类不存在");
            return result;
        }

        FavoriteCategory category = categoryOpt.get();

        if (!category.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "无权修改该分类");
            return result;
        }

        // 检查名称长度
        if (name != null && !name.isEmpty()) {
            if (name.length() > 9) {
                result.put("success", false);
                result.put("message", "收藏夹名称不能超过9个字符");
                return result;
            }
            // 检查是否与其他收藏夹重名
            if (!name.equals(category.getName()) && favoriteCategoryRepository.existsByUserIdAndName(userId, name)) {
                result.put("success", false);
                result.put("message", "收藏夹名称已存在");
                return result;
            }
            category.setName(name);
        }
        if (description != null) {
            if (description.length() > 200) {
                result.put("success", false);
                result.put("message", "收藏夹描述不能超过200个字符");
                return result;
            }
            category.setDescription(description);
        }
        if (sortOrder != null) {
            category.setSortOrder(sortOrder);
        }

        FavoriteCategory savedCategory = favoriteCategoryRepository.save(category);
        long count = favoriteRepository.countByUserIdAndCategoryId(userId, categoryId);

        result.put("success", true);
        result.put("message", "更新成功");
        result.put("category", toCategoryInfo(savedCategory, count));

        return result;
    }

    @Transactional
    public Map<String, Object> deleteCategory(Long userId, Long categoryId) {
        log.info("删除分类: userId={}, categoryId={}", userId, categoryId);

        Map<String, Object> result = new HashMap<>();

        Optional<FavoriteCategory> categoryOpt = favoriteCategoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "分类不存在");
            return result;
        }

        FavoriteCategory category = categoryOpt.get();

        if (!category.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "无权删除该分类");
            return result;
        }

        if (Boolean.TRUE.equals(category.getIsDefault())) {
            result.put("success", false);
            result.put("message", "默认收藏夹不能删除");
            return result;
        }

        FavoriteCategory defaultCategory = getOrCreateDefaultCategory(userId);
        List<Favorite> favorites = favoriteRepository.findByUserIdAndCategoryId(userId, categoryId);
        for (Favorite favorite : favorites) {
            favorite.setCategoryId(defaultCategory.getId());
            favoriteRepository.save(favorite);
        }

        favoriteCategoryRepository.delete(category);

        result.put("success", true);
        result.put("message", "删除成功，收藏已移至默认收藏夹");

        return result;
    }

    public FavoriteCategory getOrCreateDefaultCategory(Long userId) {
        List<FavoriteCategory> categories = favoriteCategoryRepository.findByUserIdOrderBySortOrderAsc(userId);
        
        for (FavoriteCategory category : categories) {
            if (Boolean.TRUE.equals(category.getIsDefault())) {
                return category;
            }
        }

        FavoriteCategory defaultCategory = new FavoriteCategory();
        defaultCategory.setUserId(userId);
        defaultCategory.setName("默认收藏夹");
        defaultCategory.setIsDefault(true);
        defaultCategory.setSortOrder(0);
        return favoriteCategoryRepository.save(defaultCategory);
    }

    private Map<String, Object> toCategoryInfo(FavoriteCategory category, long favoriteCount) {
        Map<String, Object> categoryInfo = new HashMap<>();
        categoryInfo.put("id", category.getId());
        categoryInfo.put("name", category.getName());
        categoryInfo.put("description", category.getDescription());
        categoryInfo.put("isDefault", category.getIsDefault());
        categoryInfo.put("sortOrder", category.getSortOrder());
        categoryInfo.put("favoriteCount", favoriteCount);
        categoryInfo.put("createdAt", category.getCreatedAt());
        categoryInfo.put("updatedAt", category.getUpdatedAt());
        return categoryInfo;
    }
}
