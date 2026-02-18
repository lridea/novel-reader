package com.novelreader.controller;

import com.novelreader.entity.CrawlerConfig;
import com.novelreader.repository.CrawlerConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 爬虫配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler/configs")
public class ConfigController {

    @Autowired
    private CrawlerConfigRepository configRepository;

    /**
     * 获取所有配置
     */
    @GetMapping
    public ResponseEntity<Iterable<CrawlerConfig>> getAllConfigs() {
        log.info("获取所有爬虫配置");
        return ResponseEntity.ok(configRepository.findAll());
    }

    /**
     * 根据ID获取配置
     */
    @GetMapping("/{id}")
    public ResponseEntity<CrawlerConfig> getConfigById(@PathVariable Long id) {
        log.info("获取配置: id={}", id);

        return configRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateConfig(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        log.info("更新配置: id={}, updates={}", id, updates);

        return configRepository.findById(id)
                .map(config -> {
                    // 更新允许的字段
                    if (updates.containsKey("enabled")) {
                        config.setEnabled(((Number) updates.get("enabled")).intValue());
                    }
                    if (updates.containsKey("tags")) {
                        config.setTags((String) updates.get("tags"));
                    }
                    if (updates.containsKey("crawlInterval")) {
                        config.setCrawlInterval(((Number) updates.get("crawlInterval")).intValue());
                    }
                    if (updates.containsKey("maxRetry")) {
                        config.setMaxRetry(((Number) updates.get("maxRetry")).intValue());
                    }
                    if (updates.containsKey("baseUrl")) {
                        config.setBaseUrl((String) updates.get("baseUrl"));
                    }

                    CrawlerConfig savedConfig = configRepository.save(config);

                    Map<String, Object> result = new HashMap<>();
                    result.put("success", true);
                    result.put("message", "配置更新成功");
                    result.put("config", savedConfig);

                    return ResponseEntity.ok(result);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 根据平台获取配置
     */
    @GetMapping("/platform/{platform}")
    public ResponseEntity<CrawlerConfig> getConfigByPlatform(@PathVariable String platform) {
        log.info("获取平台配置: platform={}", platform);

        CrawlerConfig config = configRepository.findByPlatform(platform);
        if (config != null) {
            return ResponseEntity.ok(config);
        }
        return ResponseEntity.notFound().build();
    }
}
