package com.novelreader.service;

import com.novelreader.BaseTest;
import com.novelreader.entity.Favorite;
import com.novelreader.entity.FavoriteCategory;
import com.novelreader.entity.Novel;
import com.novelreader.repository.FavoriteCategoryRepository;
import com.novelreader.repository.FavoriteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.*;

class FavoriteServiceTest extends BaseTest {

    @Autowired
    private FavoriteService favoriteService;

    @Autowired
    private NovelService novelService;

    @Autowired
    private FavoriteCategoryRepository favoriteCategoryRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    private static final AtomicLong novelIdCounter = new AtomicLong(System.currentTimeMillis());

    private String generateNovelId() {
        return "novel_" + novelIdCounter.incrementAndGet();
    }

    private Novel createTestNovel(String title, String platform, String novelId) {
        Novel novel = new Novel();
        novel.setTitle(title);
        novel.setAuthor("测试作者");
        novel.setPlatform(platform);
        novel.setNovelId(novelId);
        novel.setCoverUrl("https://example.com/cover.jpg");
        novel.setDescription("测试描述");
        novel.setStatus(0);
        novel.setWordCount(100000L);
        novel.setFavoriteCount(0);
        novel.setCommentCount(0);
        novel.setDeleted(0);
        return novelService.save(novel);
    }

    private FavoriteCategory createTestCategory(Long userId, String name) {
        FavoriteCategory category = new FavoriteCategory();
        category.setUserId(userId);
        category.setName(name);
        category.setIsDefault(false);
        category.setSortOrder(0);
        return favoriteCategoryRepository.save(category);
    }

    @Test
    void testAddFavorite_Success() {
        Novel novel = createTestNovel("收藏小说1", "test", generateNovelId());
        FavoriteCategory category = createTestCategory(1L, "测试收藏夹");

        Map<String, Object> result = favoriteService.addFavorite(1L, novel.getId(), category.getId(), "测试备注");

        assertTrue((Boolean) result.get("success"));
        assertEquals("收藏成功", result.get("message"));
    }

    @Test
    void testAddFavorite_NovelNotFound() {
        Map<String, Object> result = favoriteService.addFavorite(1L, 99999L, null, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("小说不存在", result.get("message"));
    }

    @Test
    void testAddFavorite_AlreadyFavorited() {
        Novel novel = createTestNovel("重复收藏", "test", generateNovelId());

        favoriteService.addFavorite(1L, novel.getId(), null, null);
        Map<String, Object> result = favoriteService.addFavorite(1L, novel.getId(), null, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("已收藏该小说到此收藏夹", result.get("message"));
    }

    @Test
    void testAddFavorite_InvalidCategory() {
        Novel novel = createTestNovel("无效收藏夹", "test", generateNovelId());

        Map<String, Object> result = favoriteService.addFavorite(1L, novel.getId(), 99999L, null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("收藏夹不存在", result.get("message"));
    }

    @Test
    void testRemoveFavorite_Success() {
        Novel novel = createTestNovel("取消收藏", "test", generateNovelId());
        favoriteService.addFavorite(1L, novel.getId(), null, null);

        Map<String, Object> result = favoriteService.removeFavorite(1L, novel.getId(), null);

        assertTrue((Boolean) result.get("success"));
        assertEquals("取消成功", result.get("message"));
    }

    @Test
    void testRemoveFavorite_NotFavorited() {
        Novel novel = createTestNovel("未收藏", "test", generateNovelId());

        Map<String, Object> result = favoriteService.removeFavorite(1L, novel.getId(), null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("未收藏该小说", result.get("message"));
    }

    @Test
    void testBatchRemoveFavorites_Success() {
        Novel novel1 = createTestNovel("批量删除1", "test", generateNovelId());
        Novel novel2 = createTestNovel("批量删除2", "test", generateNovelId());

        favoriteService.addFavorite(1L, novel1.getId(), null, null);
        favoriteService.addFavorite(1L, novel2.getId(), null, null);

        Map<String, Object> result = favoriteService.batchRemoveFavorites(
            1L, List.of(novel1.getId().intValue(), novel2.getId().intValue()));

        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("removedCount"));
    }

    @Test
    void testBatchRemoveFavorites_Empty() {
        Map<String, Object> result = favoriteService.batchRemoveFavorites(1L, List.of());

        assertFalse((Boolean) result.get("success"));
        assertEquals("未收藏这些小说", result.get("message"));
    }

    @Test
    void testGetFavoriteList() {
        Novel novel = createTestNovel("列表测试", "test", generateNovelId());
        favoriteService.addFavorite(1L, novel.getId(), null, null);

        Map<String, Object> result = favoriteService.getFavoriteList(1L, null, 0, 10, "updateTime", null);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testGetFavoriteList_WithKeyword() {
        Novel novel = createTestNovel("关键字搜索", "test", generateNovelId());
        favoriteService.addFavorite(1L, novel.getId(), null, null);

        Map<String, Object> result = favoriteService.getFavoriteList(1L, null, 0, 10, "updateTime", "关键字");

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testUpdateFavoriteNote_Success() {
        Novel novel = createTestNovel("更新备注", "test", generateNovelId());
        favoriteService.addFavorite(1L, novel.getId(), null, null);

        Map<String, Object> result = favoriteService.updateFavoriteNote(1L, novel.getId(), "新备注");

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testUpdateFavoriteNote_NotFound() {
        Map<String, Object> result = favoriteService.updateFavoriteNote(1L, 99999L, "备注");

        assertFalse((Boolean) result.get("success"));
        assertEquals("未收藏该小说", result.get("message"));
    }

    @Test
    void testCheckBatchFavorites() {
        Novel novel1 = createTestNovel("批量查询1", "test", generateNovelId());
        Novel novel2 = createTestNovel("批量查询2", "test", generateNovelId());

        favoriteService.addFavorite(1L, novel1.getId(), null, null);

        Map<String, Object> result = favoriteService.checkBatchFavorites(
            1L, novel1.getId() + "," + novel2.getId());

        assertTrue((Boolean) result.get("success"));
        Map<String, Boolean> favorites = (Map<String, Boolean>) result.get("favorites");
        assertTrue(favorites.get(novel1.getId().toString()));
        assertFalse(favorites.get(novel2.getId().toString()));
    }
}
