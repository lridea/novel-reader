package com.novelreader.repository;

import com.novelreader.entity.Novel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class NovelRepositoryTest {

    @Autowired
    private NovelRepository novelRepository;

    @BeforeEach
    void setUp() {
        Novel novel = new Novel();
        novel.setPlatform("ciweimao");
        novel.setNovelId("1001");
        novel.setTitle("测试小说");
        novel.setAuthor("测试作者");
        novel.setWordCount(1000000L);
        novel.setStatus(1);
        novel.setTags("[\"玄幻\",\"修仙\"]");
        novel.setFavoriteCount(100);
        novel.setDeleted(0);
        novelRepository.save(novel);
    }

    @Test
    public void testFindByPlatformAndNovelId() {
        Novel novel = novelRepository.findByPlatformAndNovelId("ciweimao", "1001");
        assertNotNull(novel);
        assertEquals("测试小说", novel.getTitle());
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
        assertFalse(novels.isEmpty());
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
        Page<Novel> novelPage = novelRepository.searchByKeyword("测试", pageable);

        assertNotNull(novelPage);
        assertFalse(novelPage.isEmpty());
    }

    @Test
    public void testSearchByPlatformAndKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Novel> novelPage = novelRepository.searchByPlatformAndKeyword("ciweimao", "测试", pageable);

        assertNotNull(novelPage);
        assertFalse(novelPage.isEmpty());
    }

    @Test
    public void testSearchNovels_AllParameters() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Novel> novelPage = novelRepository.searchNovels(
                "ciweimao",
                "测试",
                1,
                "玄幻",
                100000L,
                2000000L,
                0,
                1000,
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
                null,
                null,
                pageable
        );

        assertNotNull(novelPage);
    }
}
