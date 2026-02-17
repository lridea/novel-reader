package com.novelreader.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 爬虫配置实体
 */
@Data
@Entity
@Table(name = "t_crawler_config")
public class CrawlerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 平台名称：ciweimao/sf/ciyuanji/qidian
     */
    @Id
    @Column(nullable = false, length = 50)
    private String platform;

    /**
     * 平台基础URL
     */
    @Column(nullable = false)
    private String baseUrl;

    /**
     * 是否启用：0-禁用, 1-启用
     */
    @Column(defaultValue = "1")
    private Integer enabled = 1;

    /**
     * 标签列表（JSON数组）
     */
    @Column(columnDefinition = "TEXT")
    private String tags;

    /**
     * 抓取间隔（小时）
     */
    @Column(defaultValue = "2")
    private Integer intervalHours = 2;

    /**
     * 最大重试次数
     */
    @Column(defaultValue = "3")
    private Integer maxRetry = 3;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
