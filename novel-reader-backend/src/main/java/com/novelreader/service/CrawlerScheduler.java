package com.novelreader.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novelreader.crawler.BaseCrawler;
import com.novelreader.crawler.model.CrawlResult;
import com.novelreader.crawler.model.Chapter;
import com.novelreader.entity.CrawlerConfig;
import com.novelreader.entity.Novel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class CrawlerScheduler {

    @Autowired
    private List<BaseCrawler> crawlers;

    @Autowired
    private CrawlerConfigService crawlerConfigService;

    @Autowired
    private CrawlerTaskManager crawlerTaskManager;

    @Autowired
    private NovelService novelService;

    @Autowired
    private AiSummaryService aiSummaryService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Scheduled(cron = "0 0 */2 * * ?")
    public void scheduleCrawlerTask() {
        log.info("========================================");
        log.info("🦞 开始执行定时爬虫任务");
        log.info("📅 时间: {}", LocalDateTime.now());
        log.info("========================================");

        try {
            List<CrawlerConfig> configs = crawlerConfigService.findAllEnabled();
            log.info("找到 {} 个启用的爬虫配置", configs.size());

            for (CrawlerConfig config : configs) {
                dispatchCrawlerTaskAsync(config);
            }

            log.info("========================================");
            log.info("🦞 定时爬虫任务已分发");
            log.info("========================================");
        } catch (Exception e) {
            log.error("定时爬虫任务执行失败: {}", e.getMessage(), e);
        }
    }

    public void dispatchCrawlerTaskAsync(CrawlerConfig config) {
        String platform = config.getPlatform();
        
        if (crawlerTaskManager.isRunning(platform)) {
            log.info("平台 {} 已有任务在运行，跳过本次调度", platform);
            return;
        }

        CompletableFuture.runAsync(() -> dispatchCrawlerTask(config));
    }

    public void dispatchCrawlerTask(CrawlerConfig config) {
        String platform = config.getPlatform();
        log.info("开始处理平台: {}", platform);

        if (!crawlerTaskManager.tryAcquireLock(platform)) {
            log.info("平台 {} 获取锁失败，跳过", platform);
            return;
        }

        String errorMessage = null;
        boolean success = false;
        LocalDateTime crawlerStartTime = LocalDateTime.now();
        try {
            BaseCrawler crawler = findCrawler(platform);
            if (crawler == null) {
                errorMessage = "未找到平台 " + platform + " 的爬虫实现";
                log.error(errorMessage);
                return;
            }

            List<String> tags = parseTags(config.getTags());
            if (tags.isEmpty()) {
                errorMessage = "平台 " + platform + " 没有配置标签";
                log.warn(errorMessage);
                return;
            }

            log.info("平台 {} 标签: {}", platform, tags);

            LocalDateTime sinceTime = crawlerTaskManager.getLastSuccessCrawlTime(platform);
            if (sinceTime != null) {
                log.info("平台 {} 增量爬取，起始时间: {}", platform, sinceTime);
            } else {
                log.info("平台 {} 首次全量爬取", platform);
            }

            CrawlResult<List<Novel>> result = crawler.crawlNovelList(tags, sinceTime);

            if (result.isSuccess()) {
                List<Novel> novels = result.getData();
                log.info("平台 {} 抓取到 {} 本小说", platform, novels.size());

                int newCount = 0;
                int updateCount = 0;

                for (Novel novel : novels) {
                    ProcessResult processResult = processNovel(novel, crawler, sinceTime);
                    if (processResult == ProcessResult.NEW) {
                        newCount++;
                    } else if (processResult == ProcessResult.UPDATED) {
                        updateCount++;
                    }
                }

                log.info("平台 {} 处理完成: 新增 {} 本, 更新 {} 本", platform, newCount, updateCount);
                success = true;
            } else {
                errorMessage = result.getErrorMessage();
                log.error("平台 {} 抓取失败: {}", platform, errorMessage);
            }

        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error("处理平台 {} 失败: {}", platform, e.getMessage(), e);
        } finally {
            crawlerTaskManager.releaseLock(platform, success, errorMessage, crawlerStartTime);
        }
    }

    private enum ProcessResult {
        NEW, UPDATED, SKIPPED
    }

    private ProcessResult processNovel(Novel novel, BaseCrawler crawler, LocalDateTime sinceTime) {
        try {
            Novel existing = novelService.findByPlatformAndNovelId(
                novel.getPlatform(),
                novel.getNovelId()
            );

            if (existing == null) {
                log.info("首次抓取小说: {} (ID: {})", novel.getTitle(), novel.getNovelId());
                handleFirstCrawl(novel, crawler);
                return ProcessResult.NEW;
            } else {
                if (sinceTime != null && novel.getLatestUpdateTime() != null) {
                    if (!novel.getLatestUpdateTime().isAfter(sinceTime)) {
                        log.debug("小说 {} 无更新，跳过", existing.getTitle());
                        return ProcessResult.SKIPPED;
                    }
                }
                log.debug("更新小说: {} (ID: {})", novel.getTitle(), novel.getNovelId());
                handleUpdate(existing, novel);
                return ProcessResult.UPDATED;
            }

        } catch (Exception e) {
            log.error("处理小说 {} 失败: {}", novel.getTitle(), e.getMessage());
            return ProcessResult.SKIPPED;
        }
    }

    private void handleFirstCrawl(Novel novel, BaseCrawler crawler) {
        try {
            // List<Chapter> chapters = crawler.fetchChapters(novel.getNovelId(), 3);

            // if (!chapters.isEmpty()) {
            //     StringBuilder combinedContent = new StringBuilder();
            //     for (int i = 0; i < chapters.size(); i++) {
            //         Chapter chapter = chapters.get(i);
            //         combinedContent.append("第").append(i + 1).append("章 ")
            //                   .append(chapter.getTitle()).append("\n")
            //                   .append(chapter.getContent()).append("\n\n");
            //     }
            //     // TODO 首次抓取时，生成小说的概括，AI后续开发
            //     // String summary = aiSummaryService.summarize(combinedContent.toString());
            //     // novel.setFirstChaptersSummary(summary);

            //     log.info("小说 {} AI概括已生成", novel.getTitle());
            // }

            novel.setLastCrawlTime(LocalDateTime.now());
            novel.setCrawlCount(1);

            novelService.save(novel);

            log.info("小说 {} 首次抓取完成", novel.getTitle());

        } catch (Exception e) {
            log.error("首次抓取小说 {} 失败: {}", novel.getTitle(), e.getMessage());
        }
    }

    private void handleUpdate(Novel existing, Novel novel) {
        try {
            boolean needUpdate = false;

            if (novel.getLatestUpdateTime() != null &&
                !novel.getLatestUpdateTime().equals(existing.getLatestUpdateTime())) {
                log.info("小说 {} 有更新", existing.getTitle());
                needUpdate = true;
            }

            if (needUpdate) {
                existing.setTitle(novel.getTitle());
                existing.setAuthor(novel.getAuthor());
                existing.setDescription(novel.getDescription());
                existing.setCoverUrl(novel.getCoverUrl());
                existing.setLatestChapterTitle(novel.getLatestChapterTitle());
                existing.setLatestUpdateTime(novel.getLatestUpdateTime());
                existing.setLastCrawlTime(LocalDateTime.now());
                existing.setCrawlCount(existing.getCrawlCount() + 1);

                novelService.save(existing);

                log.info("小说 {} 更新完成 (第{}次抓取)",
                    existing.getTitle(), existing.getCrawlCount());
            }

        } catch (Exception e) {
            log.error("更新小说 {} 失败: {}", existing.getTitle(), e.getMessage());
        }
    }

    private BaseCrawler findCrawler(String platform) {
        return crawlers.stream()
                .filter(c -> platform.equals(c.getPlatformName()))
                .findFirst()
                .orElse(null);
    }

    private List<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("解析标签列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
