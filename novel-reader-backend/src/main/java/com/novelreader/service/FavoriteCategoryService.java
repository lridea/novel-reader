package com.novelreader.service;

import com.novelreader.entity.FavoriteCategory;
import com.novelreader.repository.FavoriteCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏分类服务
 */
@Slf4j
@Service
public class FavoriteCategoryService {

    @Autowired
    private FavoriteCategoryRepository favoriteCategoryRepository;

    /**
     * 创建分类
     */
    @Transactional
    public Map<String, Object> createCategory(Long userId, String name, String description) {
        log.info("创建分类: userId={}, name={}", userId, name);

        Map<String, Object> result = new HashMap<>();

        // 创建分类
        FavoriteCategory category = new FavoriteCategory();
        category.setUserId(userId);
        category.setName(name);
        category.setDescription(description);
        category.setSortOrder(0);

        FavoriteCategory savedCategory = favoriteCategoryRepository.save(category);

        result.put("success", true);
        result.put("message", "创建成功");
        result.put("category", toCategoryInfo(savedCategory));

        return result;
    }

    /**
     * 获取分类列表
     */
    public Map<String, Object> getCategoryList(Long userId) {
        log.info("获取分类列表: userId={}", userId);

        Map<String, Object> result = new HashMap<>();

        List<FavoriteCategory> categories = favoriteCategoryRepository.findByUserIdOrderBySortOrderAsc(userId);

        result.put("success", true);
        result.put("categories", categories.stream().map(this::toCategoryInfo).toList());

        return result;
    }

    /**
     * 更新分类
     */
    @Transactional
    public Map<String, Object> updateCategory(Long userId, Long categoryId, String name, String description, Integer sortOrder) {
        log.info("更新分类: userId={}, categoryId={}", userId, categoryId);

        Map<String, Object> result = new HashMap<>();

        // 查找分类
        Optional<FavoriteCategory> categoryOpt = favoriteCategoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "分类不存在");
            return result;
        }

        FavoriteCategory category = categoryOpt.get();

        // 验证权限
        if (!category.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "无权修改该分类");
            return result;
        }

        // 更新字段
        if (name != null && !name.isEmpty()) {
            category.setName(name);
        }
        if (description != null) {
            category.setDescription(description);
        }
        if (sortOrder != null) {
            category.setSortOrder(sortOrder);
        }

        FavoriteCategory savedCategory = favoriteCategoryRepository.save(category);

        result.put("success", true);
        result.put("message", "更新成功");
        result.put("category", toCategoryInfo(savedCategory));

        return result;
    }

    /**
     * 删除分类
     */
    @Transactional
    public Map<String, Object> deleteCategory(Long userId, Long categoryId) {
        log.info("删除分类: userId={}, categoryId={}", userId, categoryId);

        Map<String, Object> result = new HashMap<>();

        // 查找分类
        Optional<FavoriteCategory> categoryOpt = favoriteCategoryRepository.findById(categoryId);
        if (categoryOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "分类不存在");
            return result;
        }

        FavoriteCategory category = categoryOpt.get();

        // 验证权限
        if (!category.getUserId().equals(userId)) {
            result.put("success", false);
            result.put("message", "无权删除该分类");
            return result;
        }

        // 删除分类
        favoriteCategoryRepository.delete(category);

        result.put("success", true);
        result.put("message", "删除成功");

        return result;
    }

    /**
     * 转换为分类信息
     */
    private Map<String, Object> toCategoryInfo(FavoriteCategory category) {
        Map<String, Object> categoryInfo = new HashMap<>();
        categoryInfo.put("id", category.getId());
        categoryInfo.put("name", category.getName());
        categoryInfo.put("description", category.getDescription());
        categoryInfo.put("sortOrder", category.getSortOrder());
        categoryInfo.put("createdAt", category.getCreatedAt());
        categoryInfo.put("updatedAt", category.getUpdatedAt());
        return categoryInfo;
    }
}
