package com.novelreader.controller;

import com.novelreader.entity.User;
import com.novelreader.service.SensitiveWordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 敏感词Controller
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
    public Map<String, Object> getSensitiveWords(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer enabled,
            Authentication authentication
    ) {
        log.info("获取敏感词列表: page={}, size={}, category={}, enabled={}", page, size, category, enabled);

        // 检查用户是否登录
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        return sensitiveWordService.getSensitiveWords(page, size, category, enabled);
    }

    /**
     * 添加敏感词
     */
    @PostMapping
    public Map<String, Object> addSensitiveWord(
            @RequestBody Map<String, Object> request,
            Authentication authentication
    ) {
        log.info("添加敏感词: {}", request);

        // 检查用户是否登录
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        String word = (String) request.get("word");
        String category = (String) request.get("category");
        Integer severity = request.get("severity") != null ? ((Number) request.get("severity")).intValue() : 1;

        return sensitiveWordService.addSensitiveWord(word, category, severity);
    }

    /**
     * 更新敏感词
     */
    @PutMapping("/{id}")
    public Map<String, Object> updateSensitiveWord(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request,
            Authentication authentication
    ) {
        log.info("更新敏感词: id={}, {}", id, request);

        // 检查用户是否登录
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

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
    public Map<String, Object> deleteSensitiveWord(
            @PathVariable Long id,
            Authentication authentication
    ) {
        log.info("删除敏感词: id={}", id);

        // 检查用户是否登录
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        return sensitiveWordService.deleteSensitiveWord(id);
    }

    /**
     * 测试文本是否包含敏感词
     */
    @PostMapping("/test")
    public Map<String, Object> testText(
            @RequestBody Map<String, String> request,
            Authentication authentication
    ) {
        log.info("测试文本: {}", request);

        // 检查用户是否登录
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        String text = request.get("text");

        return sensitiveWordService.testText(text);
    }
}
