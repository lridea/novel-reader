package com.novelreader.service;

import com.novelreader.BaseTest;
import com.novelreader.entity.Novel;
import com.novelreader.repository.NovelRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class NovelServiceTest extends BaseTest {

    @Autowired
    private NovelService novelService;

    @Autowired
    private NovelRepository novelRepository;

    @Test
    void testSave() {
        Novel novel = new Novel();
        novel.setTitle("测试小说");
        novel.setAuthor("测试作者");
        novel.setPlatform("test");
        novel.setNovelId("12345");
        novel.setCoverUrl("https://example.com/cover.jpg");
        novel.setDescription("测试描述");
        novel.setTags("玄幻,都市");
        novel.setStatus(0);
        novel.setWordCount(100000L);
        novel.setFavoriteCount(0);
        novel.setCommentCount(0);
        novel.setDeleted(0);

        Novel saved = novelService.save(novel);

        assertNotNull(saved.getId());
        assertEquals("测试小说", saved.getTitle());
    }

    @Test
    void testFindByPlatformAndNovelId() {
        Novel novel = new Novel();
        novel.setTitle("平台小说");
        novel.setPlatform("test");
        novel.setNovelId("unique123");
        novel.setDeleted(0);
        novelService.save(novel);

        Novel found = novelService.findByPlatformAndNovelId("test", "unique123");

        assertNotNull(found);
        assertEquals("平台小说", found.getTitle());
    }

    @Test
    void testFindAll() {
        List<Novel> novels = novelService.findAll();

        assertNotNull(novels);
    }

    @Test
    void testFindByPlatform() {
        Novel novel = new Novel();
        novel.setTitle("平台测试");
        novel.setPlatform("ciweimao");
        novel.setNovelId("test001");
        novel.setDeleted(0);
        novelService.save(novel);

        List<Novel> novels = novelService.findByPlatform("ciweimao");

        assertTrue(novels.size() > 0);
        assertTrue(novels.stream().allMatch(n -> "ciweimao".equals(n.getPlatform())));
    }

    @Test
    void testDelete() {
        Novel novel = new Novel();
        novel.setTitle("待删除小说");
        novel.setPlatform("test");
        novel.setNovelId("delete001");
        novel.setDeleted(0);
        Novel saved = novelService.save(novel);

        novelService.delete(saved.getId());

        Novel deleted = novelRepository.findById(saved.getId()).orElseThrow();
        assertEquals(1, deleted.getDeleted());
    }

    @Test
    void testBatchDelete_Success() {
        Novel novel1 = new Novel();
        novel1.setTitle("批量删除1");
        novel1.setPlatform("test");
        novel1.setNovelId("batch001");
        novel1.setDeleted(0);
        Novel saved1 = novelService.save(novel1);

        Novel novel2 = new Novel();
        novel2.setTitle("批量删除2");
        novel2.setPlatform("test");
        novel2.setNovelId("batch002");
        novel2.setDeleted(0);
        Novel saved2 = novelService.save(novel2);

        Map<String, Object> result = novelService.batchDelete(List.of(saved1.getId(), saved2.getId()));

        assertTrue((Boolean) result.get("success"));
        assertEquals(2, result.get("deleteCount"));
    }

    @Test
    void testBatchDelete_EmptyIds() {
        Map<String, Object> result = novelService.batchDelete(List.of());

        assertFalse((Boolean) result.get("success"));
        assertEquals("参数错误", result.get("message"));
    }

    @Test
    void testDislikeNovel_Success() {
        Novel novel = new Novel();
        novel.setTitle("点踩测试");
        novel.setPlatform("test");
        novel.setNovelId("dislike001");
        novel.setDislikeCount(0);
        novel.setDeleted(0);
        Novel saved = novelService.save(novel);

        Map<String, Object> result = novelService.dislikeNovel(1L, saved.getId());

        assertTrue((Boolean) result.get("success"));
        assertTrue((Boolean) result.get("disliked"));
    }

    @Test
    void testDislikeNovel_AlreadyDisliked() {
        Novel novel = new Novel();
        novel.setTitle("重复点踩");
        novel.setPlatform("test");
        novel.setNovelId("dislike002");
        novel.setDislikeCount(0);
        novel.setDeleted(0);
        Novel saved = novelService.save(novel);

        novelService.dislikeNovel(1L, saved.getId());
        Map<String, Object> result = novelService.dislikeNovel(1L, saved.getId());

        assertFalse((Boolean) result.get("success"));
        assertEquals("已点踩该书籍", result.get("message"));
    }

    @Test
    void testUndislikeNovel_Success() {
        Novel novel = new Novel();
        novel.setTitle("取消点踩");
        novel.setPlatform("test");
        novel.setNovelId("undislike001");
        novel.setDislikeCount(0);
        novel.setDeleted(0);
        Novel saved = novelService.save(novel);

        novelService.dislikeNovel(1L, saved.getId());
        Map<String, Object> result = novelService.undislikeNovel(1L, saved.getId());

        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("disliked"));
    }

    @Test
    void testUndislikeNovel_NotDisliked() {
        Novel novel = new Novel();
        novel.setTitle("未点踩");
        novel.setPlatform("test");
        novel.setNovelId("undislike002");
        novel.setDislikeCount(0);
        novel.setDeleted(0);
        Novel saved = novelService.save(novel);

        Map<String, Object> result = novelService.undislikeNovel(1L, saved.getId());

        assertFalse((Boolean) result.get("success"));
        assertEquals("未点踩该书籍", result.get("message"));
    }

    @Test
    void testSearchNovels_ByKeyword() {
        Novel novel = new Novel();
        novel.setTitle("搜索关键字小说");
        novel.setAuthor("搜索作者");
        novel.setPlatform("test");
        novel.setNovelId("search001");
        novel.setDeleted(0);
        novelService.save(novel);

        Map<String, Object> result = novelService.searchNovels(0, 10, "关键字", null);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testSearchNovels_ByMinDislikeCount() {
        Novel novel = new Novel();
        novel.setTitle("高踩小说");
        novel.setPlatform("test");
        novel.setNovelId("dislikehigh001");
        novel.setDislikeCount(10);
        novel.setDeleted(0);
        novelService.save(novel);

        Map<String, Object> result = novelService.searchNovels(0, 10, null, 5);

        assertTrue((Boolean) result.get("success"));
    }
}
