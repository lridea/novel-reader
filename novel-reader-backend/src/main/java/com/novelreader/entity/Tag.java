package com.novelreader.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 标签实体
 */
@Data
@Entity
@Table(name = "t_tag", indexes = {
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_source", columnList = "source")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_name", columnNames = {"name"})
})
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 标签名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 来源: CRAWL-爬取, USER-用户添加
     */
    @Column(nullable = false, length = 20)
    private String source = "CRAWL";

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
