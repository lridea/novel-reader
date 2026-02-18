package com.novelreader.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 收藏分类实体
 */
@Data
@Entity
@Table(name = "t_favorite_category", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_sort_order", columnList = "sortOrder")
})
public class FavoriteCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 分类名称
     */
    @Column(nullable = false, length = 50)
    private String name;

    /**
     * 描述
     */
    @Column(length = 200)
    private String description;

    /**
     * 排序序号
     */
    @ColumnDefault("0")
    private Integer sortOrder = 0;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
