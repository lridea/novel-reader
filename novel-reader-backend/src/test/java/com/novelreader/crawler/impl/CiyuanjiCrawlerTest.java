package com.novelreader.crawler.impl;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CiyuanjiCrawlerTest {

    private CiyuanjiCrawler crawler;

    @BeforeEach
    void setUp() {
        crawler = new CiyuanjiCrawler();
        ReflectionTestUtils.setField(crawler, "userAgent", 
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
    }

    @Test
    void testGetPlatformName() {
        assertEquals("ciyuanji", crawler.getPlatformName());
    }

    @Test
    void testCrawlNovelDetail() {
        System.out.println("\n========== 测试爬取次元姬小说详情 ==========");
        
        String testNovelId = "29937";
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
    }

    @Test
    void testFetchChapters() {
        System.out.println("\n========== 测试爬取次元姬章节列表 ==========");
        
        String testNovelId = "29937";
        int chapterCount = 5;
        System.out.println("测试小说ID: " + testNovelId);
        System.out.println("抓取章节数: " + chapterCount);
        
        List<Chapter> chapters = crawler.fetchChapters(testNovelId, chapterCount);
        
        assertNotNull(chapters);
        System.out.println("实际抓取章节数: " + chapters.size());
        
        for (int i = 0; i < chapters.size(); i++) {
            Chapter chapter = chapters.get(i);
            System.out.println("\n--- 章节 " + (i + 1) + " ---");
            System.out.println("章节号: " + chapter.getChapter());
            System.out.println("章节ID: " + chapter.getChapterId());
            System.out.println("标题: " + chapter.getTitle());
            
            assertEquals(i + 1, chapter.getChapter());
        }
    }

    @Test
    void testParseUpdateTime() {
        System.out.println("\n========== 测试解析更新时间 ==========");
        
        String[] testTimes = {
            "2026-02-17 11:50:06",
            "2026-02-17 11:50",
            "2026/2/17 21:36:35",
            "2026/2/17",
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
    void testParseListHtml() throws IOException {
        System.out.println("\n========== 测试解析列表页HTML ==========");
        
        File htmlFile = new File("../docs/novel-reader-design/次元姬/list.html");
        if (!htmlFile.exists()) {
            System.out.println("跳过测试：HTML文件不存在 - " + htmlFile.getAbsolutePath());
            return;
        }
        
        String html = Files.readString(htmlFile.toPath());
        Document doc = Jsoup.parse(html);
        
        var novelElements = doc.select(".card_item__BZXh0");
        System.out.println("找到小说元素: " + novelElements.size());
        
        assertTrue(novelElements.size() > 0, "应该找到至少一个小说元素");
        
        for (int i = 0; i < Math.min(3, novelElements.size()); i++) {
            var element = novelElements.get(i);
            System.out.println("\n--- 小说 " + (i + 1) + " ---");
            
            var titleElement = element.select(".BookCard_title__nQGag").first();
            if (titleElement != null) {
                String title = titleElement.text().trim();
                System.out.println("标题: " + title);
                assertFalse(title.isEmpty(), "标题不应为空");
            }
            
            var authorElement = element.select(".BookCard_name1__Po00_").first();
            if (authorElement != null) {
                String author = authorElement.text().trim();
                System.out.println("作者: " + author);
            }
            
            var linkElement = element.select("a[href*=/b_d_]").first();
            if (linkElement != null) {
                String href = linkElement.attr("href");
                System.out.println("链接: " + href);
                assertTrue(href.contains("/b_d_"), "链接应包含小说ID");
            }
        }
    }

    @Test
    void testParseDetailHtml() throws IOException {
        System.out.println("\n========== 测试解析详情页HTML ==========");
        
        File htmlFile = new File("../docs/novel-reader-design/次元姬/detail.html");
        if (!htmlFile.exists()) {
            System.out.println("跳过测试：HTML文件不存在 - " + htmlFile.getAbsolutePath());
            return;
        }
        
        String html = Files.readString(htmlFile.toPath());
        Document doc = Jsoup.parse(html);
        
        var timeElement = doc.select(".book_detail_time__E3K_Y").first();
        if (timeElement != null) {
            String updateTime = timeElement.text().trim();
            System.out.println("更新时间: " + updateTime);
            assertFalse(updateTime.isEmpty(), "更新时间不应为空");
        }
        
        var tagElements = doc.select(".book_detail_tag__dIVn_");
        System.out.println("标签数量: " + tagElements.size());
        for (var tagElement : tagElements) {
            System.out.println("  标签: " + tagElement.text().trim());
        }
        
        var articleElement = doc.select(".book_detail_article__tNriO").first();
        if (articleElement != null) {
            String desc = articleElement.text().trim();
            System.out.println("简介: " + desc.substring(0, Math.min(100, desc.length())) + "...");
            assertFalse(desc.isEmpty(), "简介不应为空");
        }
        
        var totalElement = doc.select(".book_detail_total__b1b1O").first();
        if (totalElement != null) {
            String total = totalElement.text().trim();
            System.out.println("章节总数: " + total);
        }
        
        var chapterElements = doc.select(".book_detail_item__EMrK7");
        System.out.println("章节数量: " + chapterElements.size());
        assertTrue(chapterElements.size() > 0, "应该找到至少一个章节");
        
        for (int i = 0; i < Math.min(5, chapterElements.size()); i++) {
            var chapterElement = chapterElements.get(i);
            var titleElement = chapterElement.select(".book_detail_text__HlCNP").first();
            if (titleElement != null) {
                System.out.println("  章节 " + (i + 1) + ": " + titleElement.text().trim());
            }
        }
    }

    @Test
    void testMatchTags() throws Exception {
        System.out.println("\n========== 测试标签匹配 ==========");
        
        java.lang.reflect.Method matchTagsMethod = CiyuanjiCrawler.class.getDeclaredMethod(
            "matchTags", String.class, List.class);
        matchTagsMethod.setAccessible(true);
        
        String novelTags = "后宫,同人,爽文";
        
        List<String> filterTags1 = Arrays.asList("后宫", "百合");
        boolean result1 = (boolean) matchTagsMethod.invoke(crawler, novelTags, filterTags1);
        System.out.println("小说标签: " + novelTags);
        System.out.println("过滤标签: " + filterTags1);
        System.out.println("匹配结果: " + result1);
        assertTrue(result1, "应该匹配成功（包含'后宫'）");
        
        List<String> filterTags2 = Arrays.asList("百合", "变百");
        boolean result2 = (boolean) matchTagsMethod.invoke(crawler, novelTags, filterTags2);
        System.out.println("\n过滤标签: " + filterTags2);
        System.out.println("匹配结果: " + result2);
        assertFalse(result2, "应该匹配失败（不包含任何标签）");
        
        List<String> filterTags3 = Arrays.asList("爽文");
        boolean result3 = (boolean) matchTagsMethod.invoke(crawler, novelTags, filterTags3);
        System.out.println("\n过滤标签: " + filterTags3);
        System.out.println("匹配结果: " + result3);
        assertTrue(result3, "应该匹配成功（包含'爽文'）");
        
        boolean result4 = (boolean) matchTagsMethod.invoke(crawler, null, filterTags1);
        System.out.println("\n小说标签: null");
        System.out.println("匹配结果: " + result4);
        assertFalse(result4, "null标签应该匹配失败");
        
        boolean result5 = (boolean) matchTagsMethod.invoke(crawler, novelTags, new ArrayList<>());
        System.out.println("\n过滤标签: 空");
        System.out.println("匹配结果: " + result5);
        assertFalse(result5, "空过滤标签应该匹配失败");
    }
}
