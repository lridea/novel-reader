package com.novelreader.controller;

import com.novelreader.crawler.BaseCrawler;
import com.novelreader.entity.Novel;
import com.novelreader.service.CrawlerScheduler;
import com.novelreader.service.CrawlerConfigService;
import com.novelreader.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 爬虫管理Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    @Autowired
    private CrawlerScheduler crawlerScheduler;

    @Autowired
    private CrawlerConfigService crawlerConfigService;

    @Autowired
    private NovelService novelService;

    @Autowired
    private List<BaseCrawler> crawlers;

    /**
     * 手动触发爬虫任务
     */
    @PostMapping("/trigger")
    public Map<String, Object> triggerCrawler() {
        log.info("收到手动触发爬虫请求");

        Map<String, Object> result = new HashMap<>();
        try {
            // 异步执行爬虫任务
            new Thread(() -> crawlerScheduler.scheduleCrawlerTask()).start();

            result.put("success", true);
            result.put("message", "爬虫任务已启动");
        } catch (Exception e) {
            log.error("触发爬虫任务失败: {}", e.getMessage());
            result.put("success", false);
            result.put("message", "触发失败: " + e.getMessage());
        }

        return result;
    }

    /**
     * 获取所有小说
     */
    @GetMapping("/novels")
    public List<Novel> getAllNovels() {
        return novelService.findAll();
    }

    /**
     * 根据平台获取小说
     */
    @GetMapping("/novels/platform/{platform}")
    public List<Novel> getNovelsByPlatform(@PathVariable String platform) {
        return novelService.findByPlatform(platform);
    }

    /**
     * 获取所有爬虫配置
     */
    @GetMapping("/configs")
    public Object getAllConfigs() {
        return crawlerConfigService.findAll();
    }

    /**
     * 获取所有爬虫
     */
    @GetMapping("/crawlers")
    public Map<String, String> getAllCrawlers() {
        Map<String, String> result = new HashMap<>();
        for (BaseCrawler crawler : crawlers) {
            result.put(crawler.getPlatformName(), crawler.getClass().getSimpleName());
        }
        return result;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("crawlers", crawlers.size());
        result.put("timestamp", System.currentTimeMillis());
        return result;
    }
}
