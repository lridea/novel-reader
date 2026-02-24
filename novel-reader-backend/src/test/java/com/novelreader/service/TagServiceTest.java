package com.novelreader.service;

import com.novelreader.BaseTest;
import com.novelreader.entity.Novel;
import com.novelreader.entity.TagAudit;
import com.novelreader.entity.UserTag;
import com.novelreader.repository.TagAuditRepository;
import com.novelreader.repository.UserTagRepository;
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
    private TagManageService tagManageService;

    @Autowired
    private TagAuditRepository tagAuditRepository;

    @Autowired
    private UserTagRepository userTagRepository;

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
    void testAddTag_Duplicate() {
        Novel novel = createTestNovel("重复标签");
        tagService.addTag(1L, novel.getId(), "测试标签");

        Map<String, Object> result = tagService.addTag(1L, novel.getId(), "测试标签");

        assertFalse((Boolean) result.get("success"));
        assertEquals("已添加该标签，请勿重复添加", result.get("message"));
    }

    @Test
    void testGetMyTags() {
        Novel novel = createTestNovel("我的标签");
        tagService.addTag(1L, novel.getId(), "我的标签1");

        Map<String, Object> result = tagService.getMyTags(1L, 0, 10, null);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testGetPendingAudits() {
        Novel novel = createTestNovel("审核标签");
        tagService.addTag(1L, novel.getId(), "待审核标签");

        Map<String, Object> result = tagService.getPendingAudits(0, 10, 0);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testAuditTag_Approve() {
        Novel novel = createTestNovel("审核通过");
        tagService.addTag(1L, novel.getId(), "审核标签");

        TagAudit tagAudit = tagAuditRepository.findAll().get(0);

        Map<String, Object> result = tagService.auditTag(tagAudit.getId(), 1, "审核通过", 2L);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testAuditTag_Reject() {
        Novel novel = createTestNovel("审核拒绝");
        tagService.addTag(1L, novel.getId(), "拒绝标签");

        TagAudit tagAudit = tagAuditRepository.findAll().get(0);

        Map<String, Object> result = tagService.auditTag(tagAudit.getId(), 2, "不符合规范", 2L);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testAuditTag_NotFound() {
        Map<String, Object> result = tagService.auditTag(99999L, 1, "测试", 2L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("审核记录不存在", result.get("message"));
    }

    @Test
    void testBatchAuditTags() {
        Novel novel1 = createTestNovel("批量审核1");
        Novel novel2 = createTestNovel("批量审核2");

        tagService.addTag(1L, novel1.getId(), "批量标签1");
        tagService.addTag(1L, novel2.getId(), "批量标签2");

        List<TagAudit> audits = tagAuditRepository.findAll();
        List<Long> auditIds = audits.stream().map(TagAudit::getId).toList();

        Map<String, Object> result = tagService.batchAuditTags(auditIds, 1, "批量通过", 2L);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testGetAllUserTags() {
        Novel novel = createTestNovel("用户标签");
        tagService.addTag(1L, novel.getId(), "用户标签");
        TagAudit tagAudit = tagAuditRepository.findAll().get(0);
        tagService.auditTag(tagAudit.getId(), 1, "通过", 2L);

        List<String> tags = tagService.getAllUserTags();

        assertNotNull(tags);
    }

    @Test
    void testDeleteTag_Success() {
        Novel novel = createTestNovel("删除标签");
        tagService.addTag(1L, novel.getId(), "待删除标签");
        TagAudit tagAudit = tagAuditRepository.findAll().get(0);
        tagService.auditTag(tagAudit.getId(), 1, "通过", 2L);

        Map<String, Object> result = tagService.deleteTag(1L, novel.getId(), "待删除标签");

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testDeleteTag_NotFound() {
        Novel novel = createTestNovel("不存在删除");

        Map<String, Object> result = tagService.deleteTag(1L, novel.getId(), "不存在的标签");

        assertFalse((Boolean) result.get("success"));
        assertEquals("标签不存在", result.get("message"));
    }
}
