package com.novelreader.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 爬虫结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrawlResult<T> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 数据
     */
    private T data;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建成功结果
     */
    public static <T> CrawlResult<T> success(T data) {
        return new CrawlResult<>(true, data, null);
    }

    /**
     * 创建失败结果
     */
    public static <T> CrawlResult<T> failure(String errorMessage) {
        return new CrawlResult<>(false, null, errorMessage);
    }
}
