package com.novelreader.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 爬虫任务实体
 */
@Data
@Entity
@Table(name = "t_crawler_task", indexes = {
    @Index(name = "idx_platform", columnList = "platform"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_start_time", columnList = "startTime")
})
public class CrawlerTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 平台
     */
    @Column(nullable = false, length = 50)
    private String platform;

    /**
     * 任务类型：manual-手动, scheduled-定时
     */
    @Column(length = 20)
    private String taskType;

    /**
     * 任务状态：PENDING-等待中, RUNNING-运行中, SUCCESS-成功, FAILED-失败
     */
    @Column(length = 20)
    private String status = "PENDING";

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 总数
     */
    @ColumnDefault("0")
    private Integer totalCount = 0;

    /**
     * 成功数
     */
    @ColumnDefault("0")
    private Integer successCount = 0;

    /**
     * 失败数
     */
    @ColumnDefault("0")
    private Integer failCount = 0;

    /**
     * 执行日志
     */
    @Column(columnDefinition = "TEXT")
    private String logs;

    /**
     * 错误信息
     */
    @Column(length = 500)
    private String errorMessage;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
