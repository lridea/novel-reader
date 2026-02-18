package com.novelreader.controller;

import com.novelreader.service.FavoriteCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收藏分类控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/categories")
public class FavoriteCategoryController {

    @Autowired
    private FavoriteCategoryService favoriteCategoryService;

    /**
     * 创建分类
     */
    @PostMapping
    public Map<String, Object> createCategory(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        String name = request.get("name");
        String description = request.get("description");

        return favoriteCategoryService.createCategory(userId, name, description);
    }

    /**
     * 获取分类列表
     */
    @GetMapping
    public Map<String, Object> getCategoryList() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        return favoriteCategoryService.getCategoryList(userId);
    }

    /**
     * 更新分类
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateCategory(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        Integer sortOrder = request.get("sortOrder") != null ?
                Integer.valueOf(request.get("sortOrder").toString()) : null;

        return favoriteCategoryService.updateCategory(userId, id, name, description, sortOrder);
    }

    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    public Map<String, Object> deleteCategory(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        return favoriteCategoryService.deleteCategory(userId, id);
    }
}
