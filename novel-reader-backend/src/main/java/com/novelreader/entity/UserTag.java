package com.novelreader.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 用户标签实体
 */
@Data
@Entity
@Table(name = "t_user_tag", indexes = {
    @Index(name = "idx_novel_id", columnList = "novelId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_tag", columnList = "tag")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_novel_tag", columnNames = {"userId", "novelId", "tag"})
})
public class UserTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 书籍ID
     */
    @Column(nullable = false)
    private Long novelId;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 标签名称
     */
    @Column(nullable = false, length = 100)
    private String tag;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
