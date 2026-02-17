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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SfCrawlerTest {

    private SfCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new SfCrawler();
        ReflectionTestUtils.setField(crawler, "userAgent", 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    }

    @Test
    void testGetPlatformName() {
        assertEquals("sf", crawler.getPlatformName());
    }

    @Test
    void testCrawlNovelDetail() {
        System.out.println("\n========== 测试爬取SF轻小说详情 ==========");
        
        String testNovelId = "769462";
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
        System.out.println("更新时间: " + novel.getLatestUpdateTime());
        System.out.println("简介: " + (novel.getDescription() != null ? 
            novel.getDescription().substring(0, Math.min(200, novel.getDescription().length())) + "..." : "无"));
        
        assertEquals(testNovelId, novel.getNovelId());
        assertNotNull(novel.getTitle(), "小说标题不应为空");
        assertNotNull(novel.getAuthor(), "作者不应为空");
        assertNotNull(novel.getLatestUpdateTime(), "更新时间不应为空");
    }

    @Test
    void testFetchChapters() {
        System.out.println("\n========== 测试爬取SF轻小说章节列表 ==========");
        
        String testNovelId = "769462";
        int chapterCount = 5;
        System.out.println("测试小说ID: " + testNovelId);
        System.out.println("抓取章节数: " + chapterCount);
        
        List<Chapter> chapters = crawler.fetchChapters(testNovelId, chapterCount);
        
        assertNotNull(chapters);
        System.out.println("实际抓取章节数: " + chapters.size());
        
        assertFalse(chapters.isEmpty(), "应该抓取到至少一章");
        
        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            System.out.println("\n--- 章节 " + (i + 1) + " ---");
            System.out.println("章节号: " + chapter.getChapter());
            System.out.println("章节ID: " + chapter.getChapterId());
            System.out.println("标题: " + chapter.getTitle());
            
            assertEquals(i + 1, chapter.getChapter());
            assertNotNull(chapter.getTitle(), "章节标题不应为空");
            assertNotNull(chapter.getChapterId(), "章节ID不应为空");
        }
    }

    @Test
    void testParseUpdateTime() {
        System.out.println("\n========== 测试解析更新时间 ==========");
        
        String[] testTimes = {
            "2026/2/17 21:36:35",
            "2026/2/17",
            "2026-02-17 21:36:35",
            "3分钟前",
            "2小时前",
            "1天前"
        };
        
        for (String timeStr : testTimes) {
            var result = crawler.parseUpdateTime(timeStr);
            System.out.println(timeStr + " -> " + result);
            assertNotNull(result, "时间解析不应返回null: " + timeStr);
        }
    }

    @Test
    void testParseDetailHtml() throws IOException {
        System.out.println("\n========== 测试解析详情页HTML ==========");
        
        File htmlFile = new File("docs/novel-reader-design/SF轻小说/detail.html");
        if (!htmlFile.exists()) {
            System.out.println("跳过测试：HTML文件不存在");
            return;
        }
        
        String html = Files.readString(htmlFile.toPath());
        Document doc = Jsoup.parse(html);
        
        var titleElement = doc.select("h1.title .text").first();
        if (titleElement != null) {
            String title = titleElement.text().trim();
            System.out.println("标题: " + title);
            assertEquals("恶魔的祭品", title);
        }
        
        var authorElement = doc.select(".author-name span").first();
        if (authorElement != null) {
            String author = authorElement.text().trim();
            System.out.println("作者: " + author);
            assertEquals("SimonHaye", author);
        }
        
        var textRows = doc.select(".text-row .text");
        for (var textRow : textRows) {
            String text = textRow.text();
            System.out.println("信息: " + text);
            
            if (text.startsWith("类型：")) {
                String category = text.replace("类型：", "").trim();
                System.out.println("  -> 类型: " + category);
            } else if (text.startsWith("字数：")) {
                String wordInfo = text.replace("字数：", "");
                System.out.println("  -> 字数信息: " + wordInfo);
                assertTrue(wordInfo.contains("字"));
            } else if (text.startsWith("更新：")) {
                String updateTime = text.replace("更新：", "").trim();
                System.out.println("  -> 更新时间: " + updateTime);
            }
        }
        
        var descElement = doc.select(".introduce, .summary").first();
        if (descElement != null) {
            String desc = descElement.text().trim();
            System.out.println("简介: " + desc.substring(0, Math.min(100, desc.length())) + "...");
            assertFalse(desc.isEmpty(), "简介不应为空");
        }
    }

    @Test
    void testParseDirectoryHtml() throws IOException {
        System.out.println("\n========== 测试解析目录页HTML ==========");
        
        File htmlFile = new File("docs/novel-reader-design/SF轻小说/directory.html");
        if (!htmlFile.exists()) {
            System.out.println("跳过测试：HTML文件不存在");
            return;
        }
        
        String html = Files.readString(htmlFile.toPath());
        Document doc = Jsoup.parse(html);
        
        var catalogElements = doc.select(".story-catalog");
        System.out.println("找到卷数: " + catalogElements.size());
        
        for (int i = 0; i < catalogElements.size(); i++) {
            var catalog = catalogElements.get(i);
            var titleElement = catalog.select(".catalog-title").first();
            if (titleElement != null) {
                System.out.println("\n卷名: " + titleElement.text());
            }
            
            var chapterElements = catalog.select(".catalog-list li a");
            System.out.println("章节数: " + chapterElements.size());
            
            for (int j = 0; j < Math.min(5, chapterElements.size()); j++) {
                var chapterElement = chapterElements.get(j);
                String chapterTitle = chapterElement.text().trim();
                String chapterHref = chapterElement.attr("href");
                System.out.println("  章节 " + (j + 1) + ": " + chapterTitle + " -> " + chapterHref);
            }
        }
        
        var allChapterElements = doc.select(".catalog-list li a");
        assertTrue(allChapterElements.size() > 0, "应该找到至少一个章节");
    }
}
