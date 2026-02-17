package com.novelreader.crawler.impl;

import com.novelreader.crawler.model.CrawlResult;
import com.novelreader.entity.Novel;
import com.novelreader.crawler.model.Chapter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CiweimaoCrawlerTest {

    private CiweimaoCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new CiweimaoCrawler();
        ReflectionTestUtils.setField(crawler, "userAgent", 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    }

    @Test
    void testGetPlatformName() {
        assertEquals("ciweimao", crawler.getPlatformName());
    }

    @Test
    void testCrawlNovelList() {
        System.out.println("========== 测试爬取刺猬猫小说列表 ==========");
        
        CrawlResult<List<Novel>> result = crawler.crawlNovelList(List.of("全部"));
        
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        
        List<Novel> novels = result.getData();
        System.out.println("爬取到小说数量: " + novels.size());
        
        for (int i = 0; i < Math.min(5, novels.size()); i++) {
            Novel novel = novels.get(i);
            System.out.println("\n--- 小说 " + (i + 1) + " ---");
            System.out.println("ID: " + novel.getNovelId());
            System.out.println("标题: " + novel.getTitle());
            System.out.println("作者: " + novel.getAuthor());
            System.out.println("封面: " + novel.getCoverUrl());
            System.out.println("最新章节: " + novel.getLatestChapterTitle());
            System.out.println("更新时间: " + novel.getLatestUpdateTime());
            System.out.println("简介: " + (novel.getDescription() != null ? 
                novel.getDescription().substring(0, Math.min(100, novel.getDescription().length())) + "..." : "无"));
        }
    }

    @Test
    void testCrawlNovelDetail() {
        System.out.println("\n========== 测试爬取刺猬猫小说详情 ==========");
        
        String testNovelId = "100466055";
        System.out.println("测试小说ID: " + testNovelId);
        
        Novel novel = crawler.crawlNovelDetail(testNovelId);
        
        assertNotNull(novel, "小说详情不应为空");
        System.out.println("\n--- 小说详情 ---");
        System.out.println("ID: " + novel.getNovelId());
        System.out.println("标题: " + novel.getTitle());
        System.out.println("作者: " + novel.getAuthor());
        System.out.println("封面: " + novel.getCoverUrl());
        System.out.println("标签: " + novel.getTags());
        System.out.println("状态: " + novel.getStatus());
        System.out.println("字数: " + novel.getWordCount());
        System.out.println("最新章节: " + novel.getLatestChapterTitle());
        System.out.println("更新时间: " + novel.getLatestUpdateTime());
        System.out.println("简介: " + novel.getDescription());
        
        assertEquals(testNovelId, novel.getNovelId());
    }

    @Test
    void testFetchChapters() {
        System.out.println("\n========== 测试爬取刺猬猫章节列表 ==========");
        
        String testNovelId = "100466055";
        int chapterCount = 3;
        System.out.println("测试小说ID: " + testNovelId);
        System.out.println("获取章节数: " + chapterCount);
        
        List<Chapter> chapters = crawler.fetchChapters(testNovelId, chapterCount);
        
        assertNotNull(chapters);
        System.out.println("\n获取到章节数: " + chapters.size());
        
        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            System.out.println("章节 " + (i + 1) + ": " + chapter.getTitle() + " (ID: " + chapter.getChapterId() + ")");
            
            assertNotNull(chapter.getTitle(), "章节标题不应为空");
            assertNotNull(chapter.getChapterId(), "章节ID不应为空");
        }
    }

    @Test
    void testParseUpdateTime() {
        System.out.println("\n========== 测试时间解析 ==========");
        
        String[] testTimes = {
            "2026-02-03 00:32:59",
            "2026-01-15 12:30:00",
            "2025-12-01 08:00:00"
        };
        
        for (String timeStr : testTimes) {
            var result = crawler.parseUpdateTime(timeStr);
            System.out.println("解析 '" + timeStr + "' -> " + result);
            assertNotNull(result);
        }
    }

    @Test
    void testCrawlListPageWithLocalHtml() throws IOException {
        System.out.println("\n========== 使用本地HTML测试列表页解析 ==========");
        
        File listFile = new File("../docs/novel-reader-design/刺猬猫/list.html");
        if (!listFile.exists()) {
            System.out.println("本地HTML文件不存在，跳过测试 - " + listFile.getAbsolutePath());
            return;
        }
        
        String html = Files.readString(listFile.toPath());
        Document doc = Jsoup.parse(html);
        
        var novelElements = doc.select(".rank-book-list li[data-book-id]");
        System.out.println("找到小说元素数量: " + novelElements.size());
        
        if (!novelElements.isEmpty()) {
            var firstElement = novelElements.first();
            
            String bookId = firstElement.attr("data-book-id");
            System.out.println("第一本小说ID: " + bookId);
            
            var titleElement = firstElement.select(".tit a").first();
            if (titleElement != null) {
                System.out.println("标题: " + titleElement.text());
            }
            
            var authorElement = firstElement.select(".cnt p:contains(作者) a").first();
            if (authorElement != null) {
                System.out.println("作者: " + authorElement.text());
            }
            
            var updateElement = firstElement.select(".cnt p:contains(最近更新)").first();
            if (updateElement != null) {
                System.out.println("更新信息: " + updateElement.text());
            }
            
            var descElement = firstElement.select(".desc").first();
            if (descElement != null) {
                String desc = descElement.text();
                System.out.println("简介: " + (desc.length() > 100 ? desc.substring(0, 100) + "..." : desc));
            }
            
            var coverElement = firstElement.select(".cover img").first();
            if (coverElement != null) {
                System.out.println("封面: " + coverElement.attr("src"));
            }
        }
        
        assertTrue(novelElements.size() > 0, "应该找到至少一个小说元素");
    }

    @Test
    void testCrawlDetailWithLocalHtml() throws IOException {
        System.out.println("\n========== 使用本地HTML测试详情页解析 ==========");
        
        File detailFile = new File("../docs/novel-reader-design/刺猬猫/detail.html");
        if (!detailFile.exists()) {
            System.out.println("本地HTML文件不存在，跳过测试 - " + detailFile.getAbsolutePath());
            return;
        }
        
        String html = Files.readString(detailFile.toPath());
        Document doc = Jsoup.parse(html);
        
        var bookNameMeta = doc.select("meta[property=og:novel:book_name]").first();
        if (bookNameMeta != null) {
            System.out.println("书名: " + bookNameMeta.attr("content"));
        }
        
        var authorMeta = doc.select("meta[property=og:novel:author]").first();
        if (authorMeta != null) {
            System.out.println("作者: " + authorMeta.attr("content"));
        }
        
        var categoryMeta = doc.select("meta[property=og:novel:category]").first();
        if (categoryMeta != null) {
            System.out.println("分类: " + categoryMeta.attr("content"));
        }
        
        var imageMeta = doc.select("meta[property=og:image]").first();
        if (imageMeta != null) {
            System.out.println("封面: " + imageMeta.attr("content"));
        }
        
        var descMeta = doc.select("meta[property=og:description]").first();
        if (descMeta != null) {
            String desc = descMeta.attr("content");
            System.out.println("简介: " + (desc.length() > 100 ? desc.substring(0, 100) + "..." : desc));
        }
        
        var updateTimeElement = doc.select(".update-time").first();
        if (updateTimeElement != null) {
            System.out.println("更新时间: " + updateTimeElement.text());
        }
        
        var statusElement = doc.select(".update-state").first();
        if (statusElement != null) {
            System.out.println("状态: " + statusElement.text());
        }
        
        var wordCountElement = doc.select(".book-grade b:eq(2)").first();
        if (wordCountElement != null) {
            System.out.println("字数: " + wordCountElement.text());
        }
        
        var chapterElements = doc.select(".book-chapter-list li a");
        System.out.println("章节数量: " + chapterElements.size());
        
        for (int i = 0; i < Math.min(3, chapterElements.size()); i++) {
            var chapter = chapterElements.get(i);
            System.out.println("章节 " + (i + 1) + ": " + chapter.text() + " -> " + chapter.attr("href"));
        }
        
        assertNotNull(bookNameMeta, "应该找到书名meta标签");
        assertNotNull(authorMeta, "应该找到作者meta标签");
    }
}
