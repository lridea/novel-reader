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
     * 根据父评论ID分页查询回复
     */
    Page<Comment> findByParentIdAndDeletedOrderByCreatedAtAsc(
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
}
