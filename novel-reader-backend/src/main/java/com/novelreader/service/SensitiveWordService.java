package com.novelreader.service;

import com.novelreader.entity.SensitiveWord;
import com.novelreader.filter.SensitiveWordFilter;
import com.novelreader.repository.SensitiveWordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词Service
 */
@Slf4j
@Service
public class SensitiveWordService {

    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    /**
     * 获取敏感词列表（分页）
     */
    public Map<String, Object> getSensitiveWords(Integer page, Integer size, String category, Integer enabled) {
        log.info("获取敏感词列表: page={}, size={}, category={}, enabled={}", page, size, category, enabled);

        Map<String, Object> result = new HashMap<>();

        // 分页查询
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<SensitiveWord> sensitiveWordPage;

        if (category != null && !category.isEmpty()) {
            sensitiveWordPage = sensitiveWordRepository.findByCategoryAndEnabled(category, enabled, pageable);
        } else if (enabled != null) {
            sensitiveWordPage = sensitiveWordRepository.findByEnabled(enabled, pageable);
        } else {
            sensitiveWordPage = sensitiveWordRepository.findAll(pageable);
        }

        result.put("success", true);
        result.put("content", sensitiveWordPage.getContent());
        result.put("totalElements", sensitiveWordPage.getTotalElements());
        result.put("totalPages", sensitiveWordPage.getTotalPages());
        result.put("size", sensitiveWordPage.getSize());
        result.put("number", sensitiveWordPage.getNumber());

        return result;
    }

    /**
     * 添加敏感词
     */
    @Transactional
    public Map<String, Object> addSensitiveWord(String word, String category, Integer severity) {
        log.info("添加敏感词: word={}, category={}, severity={}", word, category, severity);

        Map<String, Object> result = new HashMap<>();

        // 检查敏感词是否已存在
        if (sensitiveWordRepository.findByWord(word).isPresent()) {
            result.put("success", false);
            result.put("message", "敏感词已存在");
            return result;
        }

        // 创建敏感词
        SensitiveWord sensitiveWord = new SensitiveWord();
        sensitiveWord.setWord(word);
        sensitiveWord.setCategory(category);
        sensitiveWord.setSeverity(severity != null ? severity : 1);
        sensitiveWord.setEnabled(1);
        sensitiveWord = sensitiveWordRepository.save(sensitiveWord);

        // 重新加载敏感词库
        sensitiveWordFilter.reload();

        result.put("success", true);
        result.put("message", "添加成功");
        result.put("sensitiveWord", sensitiveWord);

        return result;
    }

    /**
     * 更新敏感词
     */
    @Transactional
    public Map<String, Object> updateSensitiveWord(Long id, String word, String category, Integer severity, Integer enabled) {
        log.info("更新敏感词: id={}, word={}, category={}, severity={}, enabled={}", id, word, category, severity, enabled);

        Map<String, Object> result = new HashMap<>();

        // 检查敏感词是否存在
        if (!sensitiveWordRepository.existsById(id)) {
            result.put("success", false);
            result.put("message", "敏感词不存在");
            return result;
        }

        // 更新敏感词
        SensitiveWord sensitiveWord = sensitiveWordRepository.findById(id).get();
        if (word != null && !word.isEmpty()) {
            sensitiveWord.setWord(word);
        }
        if (category != null && !category.isEmpty()) {
            sensitiveWord.setCategory(category);
        }
        if (severity != null) {
            sensitiveWord.setSeverity(severity);
        }
        if (enabled != null) {
            sensitiveWord.setEnabled(enabled);
        }
        sensitiveWord = sensitiveWordRepository.save(sensitiveWord);

        // 重新加载敏感词库
        sensitiveWordFilter.reload();

        result.put("success", true);
        result.put("message", "更新成功");
        result.put("sensitiveWord", sensitiveWord);

        return result;
    }

    /**
     * 删除敏感词
     */
    @Transactional
    public Map<String, Object> deleteSensitiveWord(Long id) {
        log.info("删除敏感词: id={}", id);

        Map<String, Object> result = new HashMap<>();

        // 检查敏感词是否存在
        if (!sensitiveWordRepository.existsById(id)) {
            result.put("success", false);
            result.put("message", "敏感词不存在");
            return result;
        }

        // 删除敏感词
        sensitiveWordRepository.deleteById(id);

        // 重新加载敏感词库
        sensitiveWordFilter.reload();

        result.put("success", true);
        result.put("message", "删除成功");

        return result;
    }

    /**
     * 批量删除敏感词
     */
    @Transactional
    public Map<String, Object> batchDeleteSensitiveWords(List<Long> ids) {
        log.info("批量删除敏感词: ids={}", ids);

        Map<String, Object> result = new HashMap<>();

        if (ids == null || ids.isEmpty()) {
            result.put("success", false);
            result.put("message", "参数错误");
            return result;
        }

        int deleteCount = 0;
        for (Long id : ids) {
            if (sensitiveWordRepository.existsById(id)) {
                sensitiveWordRepository.deleteById(id);
                deleteCount++;
            }
        }

        // 重新加载敏感词库
        sensitiveWordFilter.reload();

        result.put("success", true);
        result.put("message", "批量删除成功");
        result.put("deleteCount", deleteCount);

        return result;
    }

    /**
     * 测试文本是否包含敏感词
     */
    public Map<String, Object> testText(String text) {
        log.info("测试文本: text={}", text);

        Map<String, Object> result = new HashMap<>();

        boolean isSensitive = sensitiveWordFilter.isSensitive(text);

        result.put("success", true);
        result.put("isSensitive", isSensitive);

        return result;
    }

    /**
     * 批量导入敏感词
     */
    public Map<String, Object> importWords(List<String> words) {
        log.info("批量导入敏感词: words={}", words.size());

        Map<String, Object> result = new HashMap<>();

        if (words == null || words.isEmpty()) {
            result.put("success", false);
            result.put("message", "敏感词列表为空");
            return result;
        }

        int successCount = 0;
        int skipCount = 0;

        for (String word : words) {
            word = word != null ? word.trim() : "";
            if (word.isEmpty()) {
                continue;
            }

            // 检查敏感词是否已存在
            if (sensitiveWordRepository.findByWord(word).isPresent()) {
                skipCount++;
                continue;
            }

            // 创建敏感词
            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(word);
            sensitiveWord.setCategory("其他");
            sensitiveWord.setSeverity(1);
            sensitiveWord.setEnabled(1);

            sensitiveWordRepository.save(sensitiveWord);
            successCount++;
        }

        // 重新加载敏感词库
        sensitiveWordFilter.reload();

        result.put("success", true);
        result.put("message", "导入成功");
        result.put("successCount", successCount);
        result.put("skipCount", skipCount);

        return result;
    }
}
