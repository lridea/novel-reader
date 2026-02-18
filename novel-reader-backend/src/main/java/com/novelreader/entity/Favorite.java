package com.novelreader.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 收藏实体
 */
@Data
@Entity
@Table(name = "t_favorite", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_novel_id", columnList = "novelId"),
    @Index(name = "idx_user_novel", columnList = "userId,novelId")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_novel", columnNames = {"userId", "novelId"})
})
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 小说ID（本系统的ID）
     */
    @Column(nullable = false)
    private Long novelId;

    /**
     * 平台
     */
    @Column(length = 50)
    private String platform;

    /**
     * 平台内小说ID
     */
    @Column(length = 100)
    private String platformNovelId;

    /**
     * 备注
     */
    @Column(length = 500)
    private String note;

    /**
     * 最新章节标题（快照）
     */
    @Column(length = 200)
    private String latestChapterTitle;

    /**
     * 最新更新时间（快照）
     */
    private LocalDateTime latestUpdateTime;

    /**
     * 是否有更新
     */
    @ColumnDefault("0")
    private Integer hasUpdate = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
