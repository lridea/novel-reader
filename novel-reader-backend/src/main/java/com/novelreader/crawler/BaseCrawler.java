package com.novelreader.crawler;

import com.novelreader.entity.Novel;
import com.novelreader.crawler.model.CrawlResult;
import com.novelreader.crawler.model.Chapter;

import java.util.List;

/**
 * 基础爬虫接口
 * 所有平台爬虫都需要实现此接口
 */
public interface BaseCrawler {

    /**
     * 抓取小说列表
     *
     * @param tags 标签列表
     * @return 抓取结果
     */
    CrawlResult<List<Novel>> crawlNovelList(List<String> tags);

    /**
     * 抓取小说详情
     *
     * @param novelId 小说ID
     * @return 小说详情
     */
    Novel crawlNovelDetail(String novelId);

    /**
     * 抓取前N章内容
     *
     * @param novelId 小说ID
     * @param count   章节数量
     * @return 章节列表
     */
    List<Chapter> fetchChapters(String novelId, int count);

    /**
     * 获取平台名称
     *
     * @return 平台名称
     */
    String getPlatformName();

    /**
     * 检查小说是否需要更新
     *
     * @param novelId 小说ID
     * @param updateTime 当前更新时间
     * @return 是否需要更新
     */
    boolean needsUpdate(String novelId, String updateTime);
}
