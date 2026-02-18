package com.novelreader.controller;

import com.novelreader.crawler.BaseCrawler;
import com.novelreader.crawler.model.CrawlResult;
import com.novelreader.entity.CrawlerConfig;
import com.novelreader.entity.Novel;
import com.novelreader.repository.NovelRepository;
import com.novelreader.service.CrawlerScheduler;
import com.novelreader.service.CrawlerConfigService;
import com.novelreader.service.CrawlerTaskManager;
import com.novelreader.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    @Autowired
    private CrawlerScheduler crawlerScheduler;

    @Autowired
    private CrawlerConfigService crawlerConfigService;

    @Autowired
    private CrawlerTaskManager crawlerTaskManager;

    @Autowired
    private NovelService novelService;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private List<BaseCrawler> crawlers;

    @PostMapping("/trigger")
    public Map<String, Object> triggerCrawler() {
        log.info("收到手动触发爬虫请求");

        Map<String, Object> result = new HashMap<>();
        try {
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

    @PostMapping("/trigger/{platform}")
    public Map<String, Object> triggerCrawlerByPlatform(@PathVariable String platform) {
        log.info("收到手动触发平台 {} 爬虫请求", platform);

        Map<String, Object> result = new HashMap<>();
        try {
            if (crawlerTaskManager.isRunning(platform)) {
                result.put("success", false);
                result.put("message", "平台 " + platform + " 已有任务在运行");
                return result;
            }

            CrawlerConfig config = crawlerConfigService.findByPlatform(platform);
            if (config == null) {
                result.put("success", false);
                result.put("message", "平台 " + platform + " 配置不存在");
                return result;
            }

            if (config.getEnabled() != 1) {
                result.put("success", false);
                result.put("message", "平台 " + platform + " 已禁用");
                return result;
            }

            new Thread(() -> crawlerScheduler.dispatchCrawlerTask(config)).start();

            result.put("success", true);
            result.put("message", "平台 " + platform + " 爬虫任务已启动");
        } catch (Exception e) {
            log.error("触发平台 {} 爬虫任务失败: {}", platform, e.getMessage());
            result.put("success", false);
            result.put("message", "触发失败: " + e.getMessage());
        }

        return result;
    }

    @GetMapping("/test/{platform}")
    public ResponseEntity<Map<String, Object>> testCrawler(@PathVariable String platform,
                                                           @RequestParam(required = false) String tag) {
        log.info("测试平台 {} 爬虫, 标签: {}", platform, tag);

        Map<String, Object> result = new HashMap<>();
        try {
            BaseCrawler crawler = crawlers.stream()
                    .filter(c -> platform.equals(c.getPlatformName()))
                    .findFirst()
                    .orElse(null);

            if (crawler == null) {
                result.put("success", false);
                result.put("message", "未找到平台 " + platform + " 的爬虫实现");
                result.put("availablePlatforms", crawlers.stream()
                        .map(BaseCrawler::getPlatformName)
                        .collect(Collectors.toList()));
                return ResponseEntity.ok(result);
            }

            String testTag = (tag != null && !tag.isEmpty()) ? tag : "动漫穿越";
            List<String> tags = List.of(testTag);

            CrawlResult<List<Novel>> crawlResult = crawler.crawlNovelList(tags);

            result.put("success", crawlResult.isSuccess());
            result.put("platform", platform);
            result.put("tag", testTag);
            result.put("count", crawlResult.getData() != null ? crawlResult.getData().size() : 0);

            if (crawlResult.isSuccess() && crawlResult.getData() != null) {
                List<Map<String, Object>> novelSummaries = crawlResult.getData().stream()
                        .limit(10)
                        .map(n -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("novelId", n.getNovelId());
                            map.put("title", n.getTitle());
                            map.put("author", n.getAuthor());
                            map.put("coverUrl", n.getCoverUrl());
                            map.put("latestChapter", n.getLatestChapterTitle());
                            map.put("updateTime", n.getLatestUpdateTime());
                            return map;
                        })
                        .collect(Collectors.toList());
                result.put("novels", novelSummaries);
            } else {
                result.put("error", crawlResult.getErrorMessage());
            }

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("测试平台 {} 爬虫失败: {}", platform, e.getMessage(), e);
            result.put("success", false);
            result.put("message", "测试失败: " + e.getMessage());
            return ResponseEntity.ok(result);
        }
    }

    @GetMapping("/status/{platform}")
    public Map<String, Object> getCrawlerStatus(@PathVariable String platform) {
        Map<String, Object> result = new HashMap<>();
        
        CrawlerConfig config = crawlerConfigService.findByPlatform(platform);
        if (config == null) {
            result.put("exists", false);
            return result;
        }

        result.put("exists", true);
        result.put("platform", config.getPlatform());
        result.put("enabled", config.getEnabled());
        result.put("isRunning", crawlerTaskManager.isRunning(platform));
        result.put("lastCrawlTime", config.getLastCrawlTime());
        result.put("lastSuccessCrawlTime", config.getLastSuccessCrawlTime());
        result.put("crawlCount", config.getCrawlCount());
        result.put("failCount", config.getFailCount());
        result.put("lastError", config.getLastErrorMessage());

        return result;
    }

    /**
     * 获取所有小说（旧接口，兼容）
     */
    @GetMapping("/novels")
    public List<Novel> getAllNovels() {
        return novelService.findAll();
    }

    /**
     * 分页查询小说（新接口，支持筛选）
     */
    @GetMapping("/novels/page")
    public ResponseEntity<Map<String, Object>> getNovelsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) String keyword) {

        log.info("分页查询小说: page={}, size={}, platform={}, keyword={}", page, size, platform, keyword);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "latestUpdateTime"));
        Page<Novel> novelPage;

        if (platform != null && !platform.isEmpty()) {
            if (keyword != null && !keyword.isEmpty()) {
                // 平台 + 关键词
                novelPage = novelRepository.searchByPlatformAndKeyword(platform, keyword, pageable);
            } else {
                // 仅平台
                novelPage = novelRepository.findByPlatformAndDeletedOrderByLatestUpdateTimeDesc(platform, 0, pageable);
            }
        } else if (keyword != null && !keyword.isEmpty()) {
            // 仅关键词
            novelPage = novelRepository.searchByKeyword(keyword, pageable);
        } else {
            // 全部
            novelPage = novelRepository.findByDeletedOrderByLatestUpdateTimeDesc(0, pageable);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("content", novelPage.getContent());
        result.put("totalElements", novelPage.getTotalElements());
        result.put("totalPages", novelPage.getTotalPages());
        result.put("size", novelPage.getSize());
        result.put("number", novelPage.getNumber());

        return ResponseEntity.ok(result);
    }

    /**
     * 根据平台获取小说（旧接口，兼容）
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
