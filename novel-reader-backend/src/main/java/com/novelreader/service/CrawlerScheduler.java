package com.novelreader.service;

import com.novelreader.crawler.BaseCrawler;
import com.novelreader.crawler.model.CrawlResult;
import com.novelreader.crawler.model.Chapter;
import com.novelreader.entity.CrawlerConfig;
import com.novelreader.entity.Novel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 爬虫调度器
 * 负责定时执行爬虫任务
 */
@Slf4j
@Service
public class CrawlerScheduler {

    @Autowired
    private List<BaseCrawler> crawlers;

    @Autowired
    private CrawlerConfigService crawlerConfigService;

    @Autowired
    private NovelService novelService;

    @Autowired
    private AiSummaryService aiSummaryService;

    /**
     * 定时任务：每2小时执行一次
     * cron: 0 0 */2 * * ? （每2小时的0分0秒执行）
     */
    @Scheduled(cron = "0 0 */2 * * ?")
    public void scheduleCrawlerTask() {
        log.info("========================================");
        log.info("🦞 开始执行定时爬虫任务");
        log.info("📅 时间: {}", java.time.LocalDateTime.now());
        log.info("========================================");

        try {
            // 获取所有启用的平台配置
            List<CrawlerConfig> configs = crawlerConfigService.findAllEnabled();

            log.info("找到 {} 个启用的爬虫配置", configs.size());

            // 分发任务到各平台爬虫
            for (CrawlerConfig config : configs) {
                dispatchCrawlerTask(config);
            }

            log.info("========================================");
            log.info("🦞 定时爬虫任务完成");
            log.info("========================================");
        } catch (Exception e) {
            log.error("定时爬虫任务执行失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 分发爬虫任务
     */
    public void dispatchCrawlerTask(CrawlerConfig config) {
        String platform = config.getPlatform();
        log.info("开始处理平台: {}", platform);

        try {
            // 查找对应平台的爬虫
            BaseCrawler crawler = findCrawler(platform);
            if (crawler == null) {
                log.error("未找到平台 {} 的爬虫实现", platform);
                return;
            }

            // 解析标签列表
            List<String> tags = parseTags(config.getTags());
            if (tags.isEmpty()) {
                log.warn("平台 {} 没有配置标签", platform);
                return;
            }

            log.info("平台 {} 标签: {}", platform, tags);

            // 执行爬虫任务
            CrawlResult<List<Novel>> result = crawler.crawlNovelList(tags);

            if (result.isSuccess()) {
                List<Novel> novels = result.getData();
                log.info("平台 {} 抓取到 {} 本小说", platform, novels.size());

                // 处理每本小说
                for (Novel novel : novels) {
                    processNovel(novel, crawler);
                }
            } else {
                log.error("平台 {} 抓取失败: {}", platform, result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("处理平台 {} 失败: {}", platform, e.getMessage(), e);
        }
    }

    /**
     * 处理单本小说
     */
    private void processNovel(Novel novel, BaseCrawler crawler) {
        try {
            // 检查小说是否已存在
            Novel existing = novelService.findByPlatformAndNovelId(
                novel.getPlatform(),
                novel.getNovelId()
            );

            if (existing == null) {
                // 首次抓取
                log.info("首次抓取小说: {} (ID: {})", novel.getTitle(), novel.getNovelId());
                handleFirstCrawl(novel, crawler);
            } else {
                // 增量更新
                log.debug("更新小说: {} (ID: {})", novel.getTitle(), novel.getNovelId());
                handleUpdate(existing, novel);
            }

        } catch (Exception e) {
            log.error("处理小说 {} 失败: {}", novel.getTitle(), e.getMessage());
        }
    }

    /**
     * 首次抓取处理
     */
    private void handleFirstCrawl(Novel novel, BaseCrawler crawler) {
        try {
            // 抓取前3章
            List<Chapter> chapters = crawler.fetchChapters(novel.getNovelId(), 3);

            if (chapters.isEmpty()) {
                log.warn("小说 {} 没有章节", novel.getTitle());
            } else {
                // 合并前3章内容
                StringBuilder combinedContent = new StringBuilder();
                for (int i = 0; i < chapters.size(); i++) {
                    Chapter chapter = chapters.get(i);
                    combinedContent.append("第").append(i + 1).append("章 ")
                              .append(chapter.getTitle()).append("\n")
                              .append(chapter.getContent()).append("\n\n");
                }

                // AI生成前3章的综合概括
                String summary = aiSummaryService.summarize(combinedContent.toString());
                novel.setFirstChaptersSummary(summary);

                log.info("小说 {} AI概括已生成", novel.getTitle());
            }

            // 设置初始信息
            novel.setLastCrawlTime(java.time.LocalDateTime.now());
            novel.setCrawlCount(1);

            // 保存小说
            novelService.save(novel);

            log.info("小说 {} 首次抓取完成", novel.getTitle());

        } catch (Exception e) {
            log.error("首次抓取小说 {} 失败: {}", novel.getTitle(), e.getMessage());
        }
    }

    /**
     * 增量更新处理
     */
    private void handleUpdate(Novel existing, Novel novel) {
        try {
            boolean needUpdate = false;

            // 检查更新时间
            if (novel.getLatestUpdateTime() != null &&
                !novel.getLatestUpdateTime().equals(existing.getLatestUpdateTime())) {
                log.info("小说 {} 有更新", existing.getTitle());
                needUpdate = true;
            }

            // 更新基本信息
            if (needUpdate) {
                existing.setTitle(novel.getTitle());
                existing.setAuthor(novel.getAuthor());
                existing.setDescription(novel.getDescription());
                existing.setCoverUrl(novel.getCoverUrl());
                existing.setLatestChapterTitle(novel.getLatestChapterTitle());
                existing.setLatestUpdateTime(novel.getLatestUpdateTime());
                existing.setLastCrawlTime(java.time.LocalDateTime.now());
                existing.setCrawlCount(existing.getCrawlCount() + 1);

                // 保存更新
                novelService.save(existing);

                log.info("小说 {} 更新完成 (第{}次抓取)",
                    existing.getTitle(), existing.getCrawlCount());
            }

        } catch (Exception e) {
            log.error("更新小说 {} 失败: {}", existing.getTitle(), e.getMessage());
        }
    }

    /**
     * 查找对应平台的爬虫
     */
    private BaseCrawler findCrawler(String platform) {
        return crawlers.stream()
                .filter(c -> platform.equals(c.getPlatformName()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析标签列表（JSON格式）
     */
    @SuppressWarnings("unchecked")
    private List<String> parseTags(String tagsJson) {
        if (tagsJson == null || tagsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // 这里需要JSON解析库
            // 暂时返回空列表
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("解析标签列表失败: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
