package com.novelreader.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 爬虫配置实体
 */
@Data
@Entity
@Table(name = "t_crawler_config", uniqueConstraints = {
    @UniqueConstraint(name = "uk_platform", columnNames = {"platform"})
})
public class CrawlerConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String platform;

    @Column(length = 500)
    private String baseUrl;

    @ColumnDefault("1")
    private Integer enabled = 1;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @ColumnDefault("7200")
    private Integer crawlInterval = 7200;

    @ColumnDefault("3")
    private Integer maxRetry = 3;

    private LocalDateTime lastCrawlTime;

    private LocalDateTime lastSuccessCrawlTime;

    @ColumnDefault("0")
    private Integer isRunning = 0;

    private String runningTaskId;

    @ColumnDefault("0")
    private Integer crawlCount = 0;

    @ColumnDefault("0")
    private Integer failCount = 0;

    private String lastErrorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
