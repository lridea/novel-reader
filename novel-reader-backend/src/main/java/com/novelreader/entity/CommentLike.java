package com.novelreader.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 评论点赞实体
 */
@Data
@Entity
@Table(name = "t_comment_like", indexes = {
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_comment_id", columnList = "commentId")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_comment", columnNames = {"userId", "commentId"})
})
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 评论ID
     */
    @Column(nullable = false)
    private Long commentId;

    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
