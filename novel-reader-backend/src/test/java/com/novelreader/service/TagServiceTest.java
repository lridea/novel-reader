package com.novelreader.service;

import com.novelreader.BaseTest;
import com.novelreader.entity.Novel;
import com.novelreader.entity.TagAudit;
import com.novelreader.repository.TagAuditRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class TagServiceTest extends BaseTest {

    @Autowired
    private TagService tagService;

    @Autowired
    private NovelService novelService;

    @Autowired
    private TagAuditRepository tagAuditRepository;

    private Novel createTestNovel(String title) {
        Novel novel = new Novel();
        novel.setTitle(title);
        novel.setAuthor("测试作者");
        novel.setPlatform("test");
        novel.setNovelId("novel_" + System.currentTimeMillis());
        novel.setStatus(0);
        novel.setWordCount(100000L);
        novel.setFavoriteCount(0);
        novel.setCommentCount(0);
        novel.setDeleted(0);
        return novelService.save(novel);
    }

    @Test
    void testAddTag_Success() {
        Novel novel = createTestNovel("添加标签小说");

        Map<String, Object> result = tagService.addTag(1L, novel.getId(), "玄幻");

        assertTrue((Boolean) result.get("success"));
        assertEquals("标签提交成功，等待审核", result.get("message"));
    }

    @Test
    void testAddTag_NovelNotFound() {
        Map<String, Object> result = tagService.addTag(1L, 99999L, "测试标签");

        assertFalse((Boolean) result.get("success"));
        assertEquals("书籍不存在", result.get("message"));
    }

    @Test
    void testGetPendingAudits() {
        Novel novel = createTestNovel("审核标签");
        tagService.addTag(1L, novel.getId(), "待审核标签");

        Map<String, Object> result = tagService.getPendingAudits(0, 10, 0);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testAuditTag_NotFound() {
        Map<String, Object> result = tagService.auditTag(99999L, 1, "测试", 2L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("审核记录不存在", result.get("message"));
    }
}
