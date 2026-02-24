package com.novelreader.service;

import com.novelreader.BaseTest;
import com.novelreader.entity.FavoriteCategory;
import com.novelreader.repository.FavoriteCategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class FavoriteCategoryServiceTest extends BaseTest {

    @Autowired
    private FavoriteCategoryService favoriteCategoryService;

    @Autowired
    private FavoriteCategoryRepository favoriteCategoryRepository;

    @Test
    void testCreateCategory_Success() {
        Map<String, Object> result = favoriteCategoryService.createCategory(1L, "新收藏夹", "测试描述");

        assertTrue((Boolean) result.get("success"));
        assertEquals("创建成功", result.get("message"));
    }

    @Test
    void testCreateCategory_EmptyName() {
        Map<String, Object> result = favoriteCategoryService.createCategory(1L, "", "描述");

        assertFalse((Boolean) result.get("success"));
        assertEquals("收藏夹名称不能为空", result.get("message"));
    }

    @Test
    void testCreateCategory_NameTooLong() {
        Map<String, Object> result = favoriteCategoryService.createCategory(1L, "这是一个很长的收藏夹名称", "描述");

        assertFalse((Boolean) result.get("success"));
        assertEquals("收藏夹名称不能超过9个字符", result.get("message"));
    }

    @Test
    void testCreateCategory_DuplicateName() {
        favoriteCategoryService.createCategory(1L, "重复名称", null);

        Map<String, Object> result = favoriteCategoryService.createCategory(1L, "重复名称", null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("收藏夹名称已存在", result.get("message"));
    }

    @Test
    void testGetCategoryList() {
        favoriteCategoryService.createCategory(1L, "测试分类1", null);
        favoriteCategoryService.createCategory(1L, "测试分类2", null);

        Map<String, Object> result = favoriteCategoryService.getCategoryList(1L);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testUpdateCategory_Success() {
        favoriteCategoryService.createCategory(1L, "原名称", null);
        FavoriteCategory category = favoriteCategoryRepository.findAll().get(0);

        Map<String, Object> result = favoriteCategoryService.updateCategory(
            1L, category.getId(), "新名称", "新描述", 1);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testUpdateCategory_NotFound() {
        Map<String, Object> result = favoriteCategoryService.updateCategory(
            1L, 99999L, "名称", "描述", 0);

        assertFalse((Boolean) result.get("success"));
        assertEquals("分类不存在", result.get("message"));
    }

    @Test
    void testUpdateCategory_DuplicateName() {
        favoriteCategoryService.createCategory(1L, "名称A", null);
        favoriteCategoryService.createCategory(1L, "名称B", null);
        FavoriteCategory category = favoriteCategoryRepository.findAll().get(0);

        Map<String, Object> result = favoriteCategoryService.updateCategory(
            1L, category.getId(), "名称B", null, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("收藏夹名称已存在", result.get("message"));
    }

    @Test
    void testDeleteCategory_Success() {
        favoriteCategoryService.createCategory(1L, "待删除", null);
        FavoriteCategory category = favoriteCategoryRepository.findAll().get(0);

        Map<String, Object> result = favoriteCategoryService.deleteCategory(1L, category.getId());

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testDeleteCategory_DefaultCategory() {
        Map<String, Object> result = favoriteCategoryService.deleteCategory(1L, 1L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("默认收藏夹不能删除", result.get("message"));
    }

    @Test
    void testDeleteCategory_NotFound() {
        Map<String, Object> result = favoriteCategoryService.deleteCategory(1L, 99999L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("分类不存在", result.get("message"));
    }
}
