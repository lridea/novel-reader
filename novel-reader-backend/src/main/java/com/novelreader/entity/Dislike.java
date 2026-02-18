package com.novelreader.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 书籍点踩实体
 */
@Data
@Entity
@Table(name = "t_dislike", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_novel_id", columnList = "novelId")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_novel", columnNames = {"userId", "novelId"})
})
public class Dislike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 书籍ID
     */
    @Column(nullable = false)
    private Long novelId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
