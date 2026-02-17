package com.novelreader.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 小说实体
 */
@Data
@Entity
@Table(name = "t_novel", indexes = {
    @Index(name = "idx_platform", columnList = "platform"),
    @Index(name = "idx_title", columnList = "title"),
    @Index(name = "idx_author", columnList = "author"),
    @Index(name = "idx_update_time", columnList = "latestUpdateTime"),
    @Index(name = "idx_status", columnList = "status")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_platform_novel", columnNames = {"platform", "novelId"})
})
public class Novel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 平台来源：ciweimao/sf/ciyuanji/qidian
     */
    @Column(nullable = false, length = 50)
    private String platform;

    /**
     * 平台内唯一ID
     */
    @Column(nullable = false, length = 100)
    private String novelId;

    /**
     * 书名
     */
    @Column(nullable = false, length = 200)
    private String title;

    /**
     * 作者
     */
    @Column(length = 100)
    private String author;

    /**
     * 简介
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * 封面URL
     */
    @Column(length = 500)
    private String coverUrl;

    /**
     * 最新章节标题
     */
    @Column(length = 200)
    private String latestChapterTitle;

    /**
     * 标签（JSON数组格式）
     */
    @Column(length = 500)
    private String tags;

    /**
     * 总字数
     */
    private Long wordCount;

    /**
     * 最新更新时间
     */
    private LocalDateTime latestUpdateTime;

    /**
     * 前3章综合概括（仅首次抓取时生成）
     */
    @Column(columnDefinition = "TEXT")
    private String firstChaptersSummary;

    /**
     * 最后抓取时间
     */
    private LocalDateTime lastCrawlTime;

    /**
     * 抓取次数
     */
    @ColumnDefault("0")
    private Integer crawlCount = 0;

    /**
     * 状态：0-停更, 1-连载, 2-完结
     */
    @ColumnDefault("1")
    private Integer status = 1;

    /**
     * 是否删除：0-否, 1-是
     */
    @ColumnDefault("0")
    private Integer deleted = 0;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Novel)) return false;
        Novel novel = (Novel) o;
        return platform.equals(novel.platform) && novelId.equals(novel.novelId);
    }

    @Override
    public int hashCode() {
        return platform.hashCode() + novelId.hashCode();
    }
}
