package com.novelreader.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 评论实体
 */
@Data
@Entity
@Table(name = "t_comment", indexes = {
    @Index(name = "idx_novel_id", columnList = "novelId"),
    @Index(name = "idx_parent_id", columnList = "parentId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_created_at", columnList = "createdAt"),
    @Index(name = "idx_novel_floor", columnList = "novelId, floor, createdAt DESC")
})
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;

    /**
     * 小说ID
     */
    @Column(nullable = false)
    private Long novelId;

    /**
     * 父评论ID（NULL表示顶层评论）
     */
    private Long parentId;

    /**
     * 楼层：1-顶层评论，2-回复评论
     */
    @ColumnDefault("1")
    private Integer floor = 1;

    /**
     * 评论内容
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 点赞数
     */
    @ColumnDefault("0")
    private Integer likeCount = 0;

    /**
     * 回复数（仅顶层评论统计）
     */
    @ColumnDefault("0")
    private Integer replyCount = 0;

    /**
     * 是否删除
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

    /**
     * 用户信息（DTO字段，不存储到数据库）
     */
    @Transient
    private UserInfo user;

    /**
     * 父评论用户信息（DTO字段，仅回复使用）
     */
    @Transient
    private UserInfo parentUser;

    /**
     * 回复列表（DTO字段，仅顶层评论使用）
     */
    @Transient
    private java.util.List<Comment> replies;

    /**
     * 当前用户是否已点赞（DTO字段）
     */
    @Transient
    private Boolean liked;

    /**
     * 当前用户是否为评论作者（DTO字段）
     */
    @Transient
    private Boolean isOwner;

    /**
     * 用户信息DTO
     */
    @Data
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
        private String avatar;
    }
}
