package com.novelreader.crawler;

import com.novelreader.entity.Novel;
import com.novelreader.crawler.model.CrawlResult;
import com.novelreader.crawler.model.Chapter;

import java.time.LocalDateTime;
import java.util.List;

public interface BaseCrawler {

    CrawlResult<List<Novel>> crawlNovelList(List<String> tags);

    CrawlResult<List<Novel>> crawlNovelList(List<String> tags, LocalDateTime sinceTime);

    Novel crawlNovelDetail(String novelId);

    List<Chapter> fetchChapters(String novelId, int count);

    String getPlatformName();

    boolean needsUpdate(String novelId, String updateTime);

    LocalDateTime parseUpdateTime(String updateTimeStr);
}
