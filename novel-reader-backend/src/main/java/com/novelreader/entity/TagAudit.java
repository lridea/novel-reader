package com.novelreader.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 标签审核实体
 */
@Data
@Entity
@Table(name = "t_tag_audit", indexes = {
    @Index(name = "idx_novel_id", columnList = "novelId"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_novel_tag", columnNames = {"userId", "novelId", "tag"})
})
public class TagAudit {

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
     * 审核状态：0-待审核, 1-已通过, 2-已拒绝
     */
    @Column(nullable = false)
    private Integer status = 0;

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
     * 审核人ID
     */
    private Long reviewedBy;

    /**
     * 审核时间
     */
    private LocalDateTime reviewedAt;

    /**
     * 拒绝原因
     */
    @Column(length = 500)
    private String reason;
}
