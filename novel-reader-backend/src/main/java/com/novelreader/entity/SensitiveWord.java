package com.novelreader.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 敏感词实体
 */
@Data
@Entity
@Table(name = "t_sensitive_word", indexes = {
    @Index(name = "idx_category", columnList = "category"),
    @Index(name = "idx_enabled", columnList = "enabled")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_word", columnNames = {"word"})
})
public class SensitiveWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 敏感词
     */
    @Column(nullable = false, length = 100, unique = true)
    private String word;

    /**
     * 分类：政治、色情、暴力、广告等
     */
    @Column(length = 50)
    private String category;

    /**
     * 严重程度：1-轻微，2-严重，3-禁止
     */
    @Column
    private Integer severity = 1;

    /**
     * 是否启用：0-禁用，1-启用
     */
    @Column
    private Integer enabled = 1;

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
