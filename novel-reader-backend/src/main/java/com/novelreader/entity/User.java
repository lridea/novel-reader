package com.novelreader.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@Entity
@Table(name = "t_user", indexes = {
    @Index(name = "idx_username", columnList = "username")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_username", columnNames = {"username"}),
    @UniqueConstraint(name = "uk_nickname", columnNames = {"nickname"})
})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名（账号名，用于登录）
     */
    @Column(nullable = false, length = 50, unique = true)
    private String username;

    /**
     * 密码（加密存储）
     */
    @Column(nullable = false, length = 100)
    private String password;

    /**
     * 邮箱（可选）
     */
    @Column(length = 100)
    private String email;

    /**
     * 昵称（用于前台展示）
     */
    @Column(nullable = false, length = 50)
    private String nickname;

    /**
     * 头像URL
     */
    @Column(length = 500)
    private String avatarUrl;

    /**
     * 角色：USER-普通用户, ADMIN-管理员
     */
    @Column(length = 20, nullable = false)
    private String role = "USER";

    /**
     * 状态：0-禁用, 1-启用
     */
    @ColumnDefault("1")
    private Integer enabled = 1;

    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
