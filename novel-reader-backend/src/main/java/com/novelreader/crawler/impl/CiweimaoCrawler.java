package com.novelreader.crawler.impl;

import com.novelreader.crawler.BaseCrawler;
import com.novelreader.crawler.model.CrawlResult;
import com.novelreader.crawler.model.Chapter;
import com.novelreader.entity.Novel;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 刺猬猫爬虫实现
 */
@Slf4j
@Component
public class CiweimaoCrawler implements BaseCrawler {

    private static final String PLATFORM = "ciweimao";
    private static final String BASE_URL = "https://mip.ciweimao.com";

    @Value("${crawler.user-agent}")
    private String userAgent;

    @Value("${crawler.timeout:30000}")
    private int timeout;

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .followRedirects(true)
            .build();

    @Override
    public String getPlatformName() {
        return PLATFORM;
    }

    @Override
    public CrawlResult<List<Novel>> crawlNovelList(List<String> tags) {
        log.info("开始抓取刺猬猫小说列表，标签: {}", tags);

        List<Novel> novels = new ArrayList<>();

        for (String tag : tags) {
            try {
                String url = BASE_URL + "/tags/" + tag;
                log.info("抓取标签URL: {}", url);

                Request request = new Request.Builder()
                        .url(url)
                        .header("User-Agent", userAgent)
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("请求失败: {}", response.code());
                        continue;
                    }

                    String html = response.body().string();
                    Document doc = Jsoup.parse(html);

                    // 解析小说列表（需要根据实际HTML结构调整）
                    Elements novelElements = doc.select(".book-list .book-item");
                    log.info("标签 {} 找到 {} 本小说", tag, novelElements.size());

                    for (Element element : novelElements) {
                        Novel novel = parseNovelElement(element, tag);
                        if (novel != null) {
                            novels.add(novel);
                        }
                    }

                    // 避免请求过快
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                log.error("抓取标签 {} 失败: {}", tag, e.getMessage());
            }
        }

        log.info("刺猬猫爬虫完成，共抓取 {} 本小说", novels.size());
        return CrawlResult.success(novels);
    }

    @Override
    public Novel crawlNovelDetail(String novelId) {
        log.info("开始抓取刺猬猫小说详情，novelId: {}", novelId);

        try {
            String url = BASE_URL + "/book/" + novelId;
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", userAgent)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("请求失败: {}", response.code());
                    return null;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                return parseNovelDetail(doc, novelId);
            }
        } catch (Exception e) {
            log.error("抓取小说详情失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public List<Chapter> fetchChapters(String novelId, int count) {
        log.info("开始抓取刺猬猫小说前 {} 章，novelId: {}", count, novelId);

        List<Chapter> chapters = new ArrayList<>();

        try {
            String url = BASE_URL + "/book/" + novelId;
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", userAgent)
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("请求失败: {}", response.code());
                    return chapters;
                }

                String html = response.body().string();
                Document doc = Jsoup.parse(html);

                // 解析章节列表（需要根据实际HTML结构调整）
                Elements chapterElements = doc.select(".chapter-list .chapter-item");

                int fetchCount = Math.min(count, chapterElements.size());
                for (int i = 0; i < fetchCount; i++) {
                    Element chapterElement = chapterElements.get(i);
                    Chapter chapter = parseChapterElement(chapterElement, i + 1);
                    if (chapter != null) {
                        chapters.add(chapter);
                    }
                }

                log.info("成功抓取 {} 章", chapters.size());
            }
        } catch (Exception e) {
            log.error("抓取章节失败: {}", e.getMessage());
        }

        return chapters;
    }

    @Override
    public boolean needsUpdate(String novelId, String updateTime) {
        // 需要对比更新时间
        // 这里需要实际获取最新更新时间进行对比
        return false;
    }

    /**
     * 解析小说元素
     */
    private Novel parseNovelElement(Element element, String tag) {
        try {
            Novel novel = new Novel();
            novel.setPlatform(PLATFORM);

            // 解析标题
            Element titleElement = element.select(".book-title").first();
            if (titleElement != null) {
                novel.setTitle(titleElement.text().trim());
            }

            // 解析作者
            Element authorElement = element.select(".book-author").first();
            if (authorElement != null) {
                novel.setAuthor(authorElement.text().trim());
            }

            // 解析简介
            Element descElement = element.select(".book-desc").first();
            if (descElement != null) {
                novel.setDescription(descElement.text().trim());
            }

            // 解析封面
            Element coverElement = element.select(".book-cover img").first();
            if (coverElement != null) {
                novel.setCoverUrl(coverElement.attr("src"));
            }

            // 解析最新章节
            Element latestChapterElement = element.select(".book-latest-chapter").first();
            if (latestChapterElement != null) {
                novel.setLatestChapterTitle(latestChapterElement.text().trim());
            }

            // 解析更新时间
            Element updateTimeElement = element.select(".book-update-time").first();
            if (updateTimeElement != null) {
                try {
                    LocalDateTime updateTime = parseUpdateTime(updateTimeElement.text().trim());
                    novel.setLatestUpdateTime(updateTime);
                } catch (Exception e) {
                    log.warn("解析更新时间失败: {}", updateTimeElement.text());
                }
            }

            // 解析novelId
            Element linkElement = element.select("a").first();
            if (linkElement != null) {
                String href = linkElement.attr("href");
                String id = extractNovelId(href);
                novel.setNovelId(id);
            }

            return novel;
        } catch (Exception e) {
            log.error("解析小说元素失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 解析小说详情
     */
    private Novel parseNovelDetail(Document doc, String novelId) {
        Novel novel = new Novel();
        novel.setPlatform(PLATFORM);
        novel.setNovelId(novelId);

        // 解析标题
        Element titleElement = doc.select(".book-title").first();
        if (titleElement != null) {
            novel.setTitle(titleElement.text().trim());
        }

        // 解析作者
        Element authorElement = doc.select(".book-author").first();
        if (authorElement != null) {
            novel.setAuthor(authorElement.text().trim());
        }

        // 解析简介
        Element descElement = doc.select(".book-desc").first();
        if (descElement != null) {
            novel.setDescription(descElement.text().trim());
        }

        return novel;
    }

    /**
     * 解析章节元素
     */
    private Chapter parseChapterElement(Element element, int chapterNum) {
        try {
            Chapter chapter = new Chapter();
            chapter.setChapter(chapterNum);

            // 解析标题
            chapter.setTitle(element.text().trim());

            // 解析内容（需要点击进入章节页面获取）
            String chapterUrl = element.attr("href");
            chapter.setChapterId(extractChapterId(chapterUrl));

            // 这里应该获取章节内容，需要另外实现
            // chapter.setContent(fetchChapterContent(chapterUrl));

            return chapter;
        } catch (Exception e) {
            log.error("解析章节元素失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从URL提取小说ID
     */
    private String extractNovelId(String url) {
        if (url == null) {
            return "";
        }
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    /**
     * 从URL提取章节ID
     */
    private String extractChapterId(String url) {
        if (url == null) {
            return "";
        }
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    /**
     * 解析更新时间
     */
    private LocalDateTime parseUpdateTime(String timeStr) {
        // 这里需要根据刺猬猫网站的实际时间格式进行解析
        // 示例：2024-01-01 12:00:00 或 2小时前
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(timeStr, formatter);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
