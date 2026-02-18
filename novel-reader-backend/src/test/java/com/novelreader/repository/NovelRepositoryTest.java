package com.novelreader.repository;

import com.novelreader.entity.Novel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * NovelRepository 单元测试
 */
@SpringBootTest
@ActiveProfiles("test")
public class NovelRepositoryTest {

    @Autowired
    private NovelRepository novelRepository;

    @Test
    public void testFindByPlatformAndNovelId() {
        Novel novel = novelRepository.findByPlatformAndNovelId("ciweimao", "100466055");
        assertNotNull(novel);
        assertEquals("修仙从系统开始", novel.getTitle());
    }

    @Test
    public void testFindByPlatform() {
        List<Novel> novels = novelRepository.findByPlatform("ciweimao");
        assertNotNull(novels);
        assertFalse(novels.isEmpty());
    }

    @Test
    public void testFindByStatus() {
        List<Novel> novels = novelRepository.findByStatus(1);
        assertNotNull(novels);
    }

    @Test
    public void testFindByDeletedOrderByLatestUpdateTimeDesc() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "latestUpdateTime"));
        Page<Novel> novelPage = novelRepository.findByDeletedOrderByLatestUpdateTimeDesc(0, pageable);

        assertNotNull(novelPage);
        assertFalse(novelPage.isEmpty());
    }

    @Test
    public void testSearchByKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Novel> novelPage = novelRepository.searchByKeyword("修仙", pageable);

        assertNotNull(novelPage);
        assertFalse(novelPage.isEmpty());
    }

    @Test
    public void testSearchByPlatformAndKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Novel> novelPage = novelRepository.searchByPlatformAndKeyword("ciweimao", "修仙", pageable);

        assertNotNull(novelPage);
        assertFalse(novelPage.isEmpty());
    }

    @Test
    public void testSearchNovels_AllParameters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Novel> novelPage = novelRepository.searchNovels(
                "ciweimao",
                "修仙",
                1,
                "玄幻",
                100000L,
                2000000L,
                pageable
        );

        assertNotNull(novelPage);
    }

    @Test
    public void testSearchNovels_OnlyStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Novel> novelPage = novelRepository.searchNovels(
                null,
                null,
                1,
                null,
                null,
                null,
                pageable
        );

        assertNotNull(novelPage);
    }

    @Test
    public void testSearchNovels_OnlyTag() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Novel> novelPage = novelRepository.searchNovels(
                null,
                null,
                null,
                "玄幻",
                null,
                null,
                pageable
        );

        assertNotNull(novelPage);
    }

    @Test
    public void testSearchNovels_WordCountRange() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Novel> novelPage = novelRepository.searchNovels(
                null,
                null,
                null,
                null,
                100000L,
                2000000L,
                pageable
        );

        assertNotNull(novelPage);
    }

    @Test
    public void testGetAllTags() {
        List<Map<String, Object>> tags = novelRepository.getAllTags();

        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        assertTrue(tags.size() <= 100);

        if (!tags.isEmpty()) {
            Map<String, Object> firstTag = tags.get(0);
            assertTrue(firstTag.containsKey("tag_name"));
            assertTrue(firstTag.containsKey("tag_count"));
        }
    }

    @Test
    public void testGetTagsByPlatform() {
        List<Map<String, Object>> tags = novelRepository.getTagsByPlatform("ciweimao");

        assertNotNull(tags);
        assertTrue(tags.size() <= 100);

        if (!tags.isEmpty()) {
            Map<String, Object> firstTag = tags.get(0);
            assertTrue(firstTag.containsKey("tag_name"));
            assertTrue(firstTag.containsKey("tag_count"));
        }
    }
}
