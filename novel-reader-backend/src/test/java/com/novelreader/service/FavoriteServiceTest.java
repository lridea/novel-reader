package com.novelreader.service;

import com.novelreader.entity.Favorite;
import com.novelreader.entity.Novel;
import com.novelreader.repository.FavoriteRepository;
import com.novelreader.repository.NovelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * 收藏服务测试
 */
@ExtendWith(MockitoExtension.class)
class FavoriteServiceTest {

    @Mock
    private FavoriteRepository favoriteRepository;

    @Mock
    private NovelRepository novelRepository;

    @InjectMocks
    private FavoriteService favoriteService;

    private Novel testNovel;
    private Favorite testFavorite;

    @BeforeEach
    void setUp() {
        testNovel = new Novel();
        testNovel.setId(1L);
        testNovel.setNovelId("test-novel-id");
        testNovel.setPlatform("ciweimao");
        testNovel.setTitle("测试小说");
        testNovel.setAuthor("测试作者");
        testNovel.setCoverUrl("https://example.com/cover.jpg");
        testNovel.setDescription("测试小说简介");
        testNovel.setLatestChapterTitle("第一章");
        testNovel.setLatestUpdateTime(LocalDateTime.now());

        testFavorite = new Favorite();
        testFavorite.setId(1L);
        testFavorite.setUserId(1L);
        testFavorite.setNovelId(1L);
        testFavorite.setPlatform("ciweimao");
        testFavorite.setPlatformNovelId("test-novel-id");
        testFavorite.setNote("测试备注");
        testFavorite.setLatestChapterTitle("第一章");
        testFavorite.setLatestUpdateTime(LocalDateTime.now());
        testFavorite.setHasUpdate(0);
        testFavorite.setCreatedAt(LocalDateTime.now());
        testFavorite.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testAddFavorite_Success() {
        // Given
        Long userId = 1L;
        Long novelId = 1L;
        String note = "测试备注";

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(testNovel));
        when(favoriteRepository.existsByUserIdAndNovelId(userId, novelId)).thenReturn(false);
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);

        // When
        Map<String, Object> result = favoriteService.addFavorite(userId, novelId, note);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("收藏成功", result.get("message"));
        assertNotNull(result.get("favorite"));

        verify(novelRepository, atLeastOnce()).findById(novelId);
        verify(favoriteRepository).existsByUserIdAndNovelId(userId, novelId);
        verify(favoriteRepository).save(any(Favorite.class));
    }

    @Test
    void testAddFavorite_NovelNotFound() {
        // Given
        Long userId = 1L;
        Long novelId = 999L;
        String note = "测试备注";

        when(novelRepository.findById(novelId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = favoriteService.addFavorite(userId, novelId, note);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("小说不存在", result.get("message"));

        verify(novelRepository).findById(novelId);
        verify(favoriteRepository, never()).existsByUserIdAndNovelId(anyLong(), anyLong());
    }

    @Test
    void testAddFavorite_AlreadyFavorited() {
        // Given
        Long userId = 1L;
        Long novelId = 1L;
        String note = "测试备注";

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(testNovel));
        when(favoriteRepository.existsByUserIdAndNovelId(userId, novelId)).thenReturn(true);

        // When
        Map<String, Object> result = favoriteService.addFavorite(userId, novelId, note);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("已收藏该小说", result.get("message"));

        verify(novelRepository).findById(novelId);
        verify(favoriteRepository).existsByUserIdAndNovelId(userId, novelId);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }

    @Test
    void testRemoveFavorite_Success() {
        // Given
        Long userId = 1L;
        Long novelId = 1L;

        when(favoriteRepository.existsByUserIdAndNovelId(userId, novelId)).thenReturn(true);
        doNothing().when(favoriteRepository).deleteByUserIdAndNovelId(userId, novelId);

        // When
        Map<String, Object> result = favoriteService.removeFavorite(userId, novelId);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("取消成功", result.get("message"));

        verify(favoriteRepository).existsByUserIdAndNovelId(userId, novelId);
        verify(favoriteRepository).deleteByUserIdAndNovelId(userId, novelId);
    }

    @Test
    void testRemoveFavorite_NotFavorited() {
        // Given
        Long userId = 1L;
        Long novelId = 999L;

        when(favoriteRepository.existsByUserIdAndNovelId(userId, novelId)).thenReturn(false);

        // When
        Map<String, Object> result = favoriteService.removeFavorite(userId, novelId);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("未收藏该小说", result.get("message"));

        verify(favoriteRepository).existsByUserIdAndNovelId(userId, novelId);
        verify(favoriteRepository, never()).deleteByUserIdAndNovelId(anyLong(), anyLong());
    }

    @Test
    void testGetFavoriteList_Success() {
        // Given
        Long userId = 1L;
        int page = 0;
        int size = 10;
        List<Favorite> favorites = List.of(testFavorite);
        Page<Favorite> favoritePage = new PageImpl<>(favorites, PageRequest.of(page, size), 1);

        when(favoriteRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(favoritePage);

        // When
        Map<String, Object> result = favoriteService.getFavoriteList(userId, page, size);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("content"));
        assertEquals(1L, result.get("totalElements"));
        assertEquals(1, result.get("totalPages"));
        assertEquals(size, result.get("size"));
        assertEquals(page, result.get("number"));

        verify(favoriteRepository).findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class));
    }

    @Test
    void testUpdateFavoriteNote_Success() {
        // Given
        Long userId = 1L;
        Long novelId = 1L;
        String newNote = "新备注";

        when(favoriteRepository.findByUserIdAndNovelId(userId, novelId))
                .thenReturn(Optional.of(testFavorite));
        when(favoriteRepository.save(any(Favorite.class))).thenReturn(testFavorite);

        // When
        Map<String, Object> result = favoriteService.updateFavoriteNote(userId, novelId, newNote);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals("更新成功", result.get("message"));
        assertNotNull(result.get("favorite"));

        verify(favoriteRepository).findByUserIdAndNovelId(userId, novelId);
        verify(favoriteRepository).save(testFavorite);
    }

    @Test
    void testUpdateFavoriteNote_NotFavorited() {
        // Given
        Long userId = 1L;
        Long novelId = 999L;
        String newNote = "新备注";

        when(favoriteRepository.findByUserIdAndNovelId(userId, novelId))
                .thenReturn(Optional.empty());

        // When
        Map<String, Object> result = favoriteService.updateFavoriteNote(userId, novelId, newNote);

        // Then
        assertNotNull(result);
        assertFalse((Boolean) result.get("success"));
        assertEquals("未收藏该小说", result.get("message"));

        verify(favoriteRepository).findByUserIdAndNovelId(userId, novelId);
        verify(favoriteRepository, never()).save(any(Favorite.class));
    }
}
