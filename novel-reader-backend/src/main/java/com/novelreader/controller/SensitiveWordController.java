package com.novelreader.controller;

import com.novelreader.service.SensitiveWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 敏感词Controller（仅管理员可访问）
 */
@Slf4j
@RestController
@RequestMapping("/api/sensitive-words")
public class SensitiveWordController {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    /**
     * 获取敏感词列表（分页）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> getSensitiveWords(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer enabled
    ) {
        log.info("获取敏感词列表: page={}, size={}, category={}, enabled={}", page, size, category, enabled);
        return sensitiveWordService.getSensitiveWords(page, size, category, enabled);
    }

    /**
     * 添加敏感词
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> addSensitiveWord(@RequestBody Map<String, Object> request) {
        log.info("添加敏感词: {}", request);

        String word = (String) request.get("word");
        String category = (String) request.get("category");
        Integer severity = request.get("severity") != null ? ((Number) request.get("severity")).intValue() : 1;

        return sensitiveWordService.addSensitiveWord(word, category, severity);
    }

    /**
     * 更新敏感词
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> updateSensitiveWord(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        log.info("更新敏感词: id={}, {}", id, request);

        String word = (String) request.get("word");
        String category = (String) request.get("category");
        Integer severity = request.get("severity") != null ? ((Number) request.get("severity")).intValue() : null;
        Integer enabled = request.get("enabled") != null ? ((Number) request.get("enabled")).intValue() : null;

        return sensitiveWordService.updateSensitiveWord(id, word, category, severity, enabled);
    }

    /**
     * 删除敏感词
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> deleteSensitiveWord(@PathVariable Long id) {
        log.info("删除敏感词: id={}", id);
        return sensitiveWordService.deleteSensitiveWord(id);
    }

    /**
     * 批量删除敏感词
     */
    @DeleteMapping("/batch")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> batchDeleteSensitiveWords(@RequestBody Map<String, Object> request) {
        log.info("批量删除敏感词: {}", request);

        @SuppressWarnings("unchecked")
        List<Long> ids = (List<Long>) request.get("ids");

        return sensitiveWordService.batchDeleteSensitiveWords(ids);
    }

    /**
     * 测试文本是否包含敏感词
     */
    @PostMapping("/test")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> testText(@RequestBody Map<String, String> request) {
        log.info("测试文本: {}", request);

        String text = request.get("text");

        return sensitiveWordService.testText(text);
    }

    /**
     * 导入敏感词库
     */
    @PostMapping("/import")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> importWords(@RequestBody Map<String, Object> request) {
        log.info("导入敏感词库: {}", request);

        @SuppressWarnings("unchecked")
        List<String> words = (List<String>) request.get("words");
        if (words != null && !words.isEmpty()) {
            return sensitiveWordService.importWords(words);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "参数错误");
        return result;
    }
}
