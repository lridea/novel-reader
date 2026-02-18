package com.novelreader.service;

import com.novelreader.entity.FavoriteCategory;
import com.novelreader.repository.FavoriteCategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 收藏分类服务测试
 */
@ExtendWith(MockitoExtension.class)
class FavoriteCategoryServiceTest {

    @Mock
    private FavoriteCategoryRepository favoriteCategoryRepository;

    @InjectMocks
    private FavoriteCategoryService favoriteCategoryService;

    private FavoriteCategory testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new FavoriteCategory();
        testCategory.setId(1L);
        testCategory.setUserId(1L);
        testCategory.setName("玄幻小说");
        testCategory.setDescription("玄幻小说分类");
        testCategory.setSortOrder(0);
        testCategory.setCreatedAt(LocalDateTime.now());
        testCategory.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateCategory_Success() {
        // Given
        Long userId = 1L;
        String name = "玄幻小说";
        String description = "玄幻小说分类";

        when(favoriteCategoryRepository.save(any(FavoriteCategory.class)))
                .thenReturn(testCategory);

        // When
        Map<String, Object> result = favoriteCategoryService.createCategory(userId, name, description);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("创建成功", result.get("message"));
        assertNotNull(result.get("category"));

        verify(favoriteCategoryRepository).save(any(FavoriteCategory.class));
    }

    @Test
    void testGetCategoryList_Success() {
        // Given
        Long userId = 1L;
        List<FavoriteCategory> categories = List.of(testCategory);

        when(favoriteCategoryRepository.findByUserIdOrderBySortOrderAsc(userId))
                .thenReturn(categories);

        // When
        Map<String, Object> result = favoriteCategoryService.getCategoryList(userId);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("categories"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> categoryList = (List<Map<String, Object>>) result.get("categories");
        assertEquals(1, categoryList.size());

        verify(favoriteCategoryRepository).findByUserIdOrderBySortOrderAsc(userId);
    }

    @Test
    void testUpdateCategory_Success() {
        // Given
        Long userId = 1L;
        Long categoryId = 1L;
        String newName = "玄幻修仙";
        String newDescription = "玄幻修仙分类";
        Integer newSortOrder = 1;

        when(favoriteCategoryRepository.findById(categoryId))
                .thenReturn(Optional.of(testCategory));
        when(favoriteCategoryRepository.save(any(FavoriteCategory.class)))
                .thenReturn(testCategory);

        // When
        Map<String, Object> result = favoriteCategoryService.updateCategory(
                userId, categoryId, newName, newDescription, newSortOrder);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("更新成功", result.get("message"));
        assertNotNull(result.get("category"));

        verify(favoriteCategoryRepository).findById(categoryId);
        verify(favoriteCategoryRepository).save(testCategory);
    }

    @Test
    void testUpdateCategory_CategoryNotFound() {
        // Given
        Long userId = 1L;
        Long categoryId = 999L;
        String newName = "玄幻修仙";
        String newDescription = "玄幻修仙分类";

        when(favoriteCategoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        // When
        Map<String, Object> result = favoriteCategoryService.updateCategory(
                userId, categoryId, newName, newDescription, null);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("分类不存在", result.get("message"));

        verify(favoriteCategoryRepository).findById(categoryId);
        verify(favoriteCategoryRepository, never()).save(any(FavoriteCategory.class));
    }

    @Test
    void testUpdateCategory_NoPermission() {
        // Given
        Long userId = 2L; // Different user
        Long categoryId = 1L;
        String newName = "玄幻修仙";

        when(favoriteCategoryRepository.findById(categoryId))
                .thenReturn(Optional.of(testCategory));

        // When
        Map<String, Object> result = favoriteCategoryService.updateCategory(
                userId, categoryId, newName, null, null);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("无权修改该分类", result.get("message"));

        verify(favoriteCategoryRepository).findById(categoryId);
        verify(favoriteCategoryRepository, never()).save(any(FavoriteCategory.class));
    }

    @Test
    void testUpdateCategory_UpdateNameOnly() {
        // Given
        Long userId = 1L;
        Long categoryId = 1L;
        String newName = "玄幻修仙";

        when(favoriteCategoryRepository.findById(categoryId))
                .thenReturn(Optional.of(testCategory));
        when(favoriteCategoryRepository.save(any(FavoriteCategory.class)))
                .thenReturn(testCategory);

        // When
        Map<String, Object> result = favoriteCategoryService.updateCategory(
                userId, categoryId, newName, null, null);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));

        verify(favoriteCategoryRepository).findById(categoryId);
        verify(favoriteCategoryRepository).save(testCategory);
    }

    @Test
    void testDeleteCategory_Success() {
        // Given
        Long userId = 1L;
        Long categoryId = 1L;

        when(favoriteCategoryRepository.findById(categoryId))
                .thenReturn(Optional.of(testCategory));
        doNothing().when(favoriteCategoryRepository).delete(any(FavoriteCategory.class));

        // When
        Map<String, Object> result = favoriteCategoryService.deleteCategory(userId, categoryId);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("删除成功", result.get("message"));

        verify(favoriteCategoryRepository).findById(categoryId);
        verify(favoriteCategoryRepository).delete(testCategory);
    }

    @Test
    void testDeleteCategory_CategoryNotFound() {
        // Given
        Long userId = 1L;
        Long categoryId = 999L;

        when(favoriteCategoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        // When
        Map<String, Object> result = favoriteCategoryService.deleteCategory(userId, categoryId);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("分类不存在", result.get("message"));

        verify(favoriteCategoryRepository).findById(categoryId);
        verify(favoriteCategoryRepository, never()).delete(any(FavoriteCategory.class));
    }

    @Test
    void testDeleteCategory_NoPermission() {
        // Given
        Long userId = 2L; // Different user
        Long categoryId = 1L;

        when(favoriteCategoryRepository.findById(categoryId))
                .thenReturn(Optional.of(testCategory));

        // When
        Map<String, Object> result = favoriteCategoryService.deleteCategory(userId, categoryId);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("无权删除该分类", result.get("message"));

        verify(favoriteCategoryRepository).findById(categoryId);
        verify(favoriteCategoryRepository, never()).delete(any(FavoriteCategory.class));
    }
}
