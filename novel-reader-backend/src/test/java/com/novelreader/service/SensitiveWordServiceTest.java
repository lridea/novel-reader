package com.novelreader.service;

import com.novelreader.BaseTest;
import com.novelreader.entity.SensitiveWord;
import com.novelreader.repository.SensitiveWordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveWordServiceTest extends BaseTest {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    @Test
    void testGetSensitiveWords() {
        sensitiveWordService.addSensitiveWord("测试词", "测试分类", 1);

        Map<String, Object> result = sensitiveWordService.getSensitiveWords(0, 10, null, null);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testAddSensitiveWord_Success() {
        Map<String, Object> result = sensitiveWordService.addSensitiveWord("新增敏感词", "政治", 2);

        assertTrue((Boolean) result.get("success"));
        assertEquals("添加成功", result.get("message"));
    }

    @Test
    void testAddSensitiveWord_AlreadyExists() {
        sensitiveWordService.addSensitiveWord("重复词", "分类", 1);

        Map<String, Object> result = sensitiveWordService.addSensitiveWord("重复词", "分类", 1);

        assertFalse((Boolean) result.get("success"));
        assertEquals("敏感词已存在", result.get("message"));
    }

    @Test
    void testUpdateSensitiveWord_Success() {
        sensitiveWordService.addSensitiveWord("待更新", "旧分类", 1);
        SensitiveWord word = sensitiveWordRepository.findAll().get(0);

        Map<String, Object> result = sensitiveWordService.updateSensitiveWord(
            word.getId(), "已更新", "新分类", 2, 1);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testUpdateSensitiveWord_NotFound() {
        Map<String, Object> result = sensitiveWordService.updateSensitiveWord(
            99999L, "词", "分类", 1, 1);

        assertFalse((Boolean) result.get("success"));
        assertEquals("敏感词不存在", result.get("message"));
    }

    @Test
    void testDeleteSensitiveWord_Success() {
        sensitiveWordService.addSensitiveWord("待删除", "分类", 1);
        SensitiveWord word = sensitiveWordRepository.findAll().get(0);

        Map<String, Object> result = sensitiveWordService.deleteSensitiveWord(word.getId());

        assertTrue((Boolean) result.get("success"));
        assertEquals("删除成功", result.get("message"));
    }

    @Test
    void testDeleteSensitiveWord_NotFound() {
        Map<String, Object> result = sensitiveWordService.deleteSensitiveWord(99999L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("敏感词不存在", result.get("message"));
    }

    @Test
    void testBatchDeleteSensitiveWords() {
        sensitiveWordService.addSensitiveWord("批量1", "分类", 1);
        sensitiveWordService.addSensitiveWord("批量2", "分类", 1);

        List<SensitiveWord> words = sensitiveWordRepository.findAll();
        List<Long> ids = words.stream().map(SensitiveWord::getId).toList();

        Map<String, Object> result = sensitiveWordService.batchDeleteSensitiveWords(ids);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testTestText_Sensitive() {
        sensitiveWordService.addSensitiveWord("敏感词", "测试", 1);

        Map<String, Object> result = sensitiveWordService.testText("这是一条包含敏感词的文本");

        assertTrue((Boolean) result.get("success"));
        assertTrue((Boolean) result.get("isSensitive"));
    }

    @Test
    void testTestText_NotSensitive() {
        Map<String, Object> result = sensitiveWordService.testText("这是一条正常文本");

        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("isSensitive"));
    }

    @Test
    void testImportWords() {
        Map<String, Object> result = sensitiveWordService.importWords(List.of("导入词1", "导入词2", "导入词3"));

        assertTrue((Boolean) result.get("success"));
    }
}
