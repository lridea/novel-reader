package com.novelreader.repository;

import com.novelreader.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 评论Repository
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    /**
     * 根据小说ID和楼层分页查询评论
     */
    Page<Comment> findByNovelIdAndFloorAndDeletedOrderByCreatedAtDesc(
        Long novelId,
        Integer floor,
        Integer deleted,
        Pageable pageable
    );

    /**
     * 根据小说ID和楼层分页查询评论（按点赞数降序）
     */
    Page<Comment> findByNovelIdAndFloorAndDeletedOrderByLikeCountDesc(
        Long novelId,
        Integer floor,
        Integer deleted,
        Pageable pageable
    );

    /**
     * 根据父评论ID分页查询回复（按时间升序）
     */
    Page<Comment> findByParentIdAndDeletedOrderByCreatedAtAsc(
        Long parentId,
        Integer deleted,
        Pageable pageable
    );

    /**
     * 根据父评论ID分页查询回复（按点赞数降序）
     */
    Page<Comment> findByParentIdAndDeletedOrderByLikeCountDesc(
        Long parentId,
        Integer deleted,
        Pageable pageable
    );

    /**
     * 统计小说的顶层评论数
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.novelId = :novelId AND c.floor = 1 AND c.deleted = 0")
    Long countTopLevelCommentsByNovelId(@Param("novelId") Long novelId);

    /**
     * 统计父评论的回复数
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentId = :parentId AND c.deleted = 0")
    Long countRepliesByParentId(@Param("parentId") Long parentId);

    /**
     * 原子操作：回复数+1
     */
    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount + 1 WHERE c.id = :commentId")
    @Transactional
    void incrementReplyCount(@Param("commentId") Long commentId);

    /**
     * 原子操作：回复数-1
     */
    @Modifying
    @Query("UPDATE Comment c SET c.replyCount = c.replyCount - 1 WHERE c.id = :commentId AND c.replyCount > 0")
    @Transactional
    void decrementReplyCount(@Param("commentId") Long commentId);

    /**
     * 原子操作：点赞数+1
     */
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount + 1 WHERE c.id = :commentId")
    @Transactional
    void incrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 原子操作：点赞数-1
     */
    @Modifying
    @Query("UPDATE Comment c SET c.likeCount = c.likeCount - 1 WHERE c.id = :commentId AND c.likeCount > 0")
    @Transactional
    void decrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 统计在指定时间之前发布的顶层评论数量（用于计算楼层号）
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.novelId = :novelId AND c.floor = 1 AND c.deleted = 0 AND c.createdAt < :createdAt")
    Long countCommentsBeforeTime(@Param("novelId") Long novelId, @Param("createdAt") java.time.LocalDateTime createdAt);

    /**
     * 统计在指定时间之前发布的回复数量（用于计算回复楼层号）
     */
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.parentId = :parentId AND c.deleted = 0 AND c.createdAt < :createdAt")
    Long countRepliesBeforeTime(@Param("parentId") Long parentId, @Param("createdAt") java.time.LocalDateTime createdAt);

    /**
     * 直接查询评论的点赞数（绕过缓存）
     */
    @Query("SELECT c.likeCount FROM Comment c WHERE c.id = :commentId")
    Integer getLikeCountById(@Param("commentId") Long commentId);

    /**
     * 统计用户的评论数量
     */
    long countByUserIdAndDeleted(Long userId, Integer deleted);
}
