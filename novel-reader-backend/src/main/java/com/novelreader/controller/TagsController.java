package com.novelreader.controller;

import com.novelreader.repository.NovelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签Controller（仅管理员可访问）
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler/novels/tags")
public class TagsController {

    @Autowired
    private NovelRepository novelRepository;

    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllTags() {
        log.info("获取所有标签");

        try {
            List<Map<String, Object>> tags = novelRepository.getAllTags();

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("tags", tags);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取标签列表失败: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取标签列表失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    /**
     * 根据平台获取标签
     *
     * @param platform 平台
     * @return 标签列表
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/platform/{platform}")
    public ResponseEntity<Map<String, Object>> getTagsByPlatform(@PathVariable String platform) {
        log.info("获取平台 {} 的标签", platform);

        try {
            List<Map<String, Object>> tags = novelRepository.getTagsByPlatform(platform);

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("platform", platform);
            result.put("tags", tags);

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("获取平台 {} 标签列表失败: {}", platform, e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取标签列表失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }
}
