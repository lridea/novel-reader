package com.novelreader.repository;

import com.novelreader.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 评论点赞Repository
 */
@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    /**
     * 检查用户是否已点赞评论
     */
    Optional<CommentLike> findByUserIdAndCommentId(Long userId, Long commentId);

    /**
     * 检查用户是否已点赞评论（是否存在）
     */
    boolean existsByUserIdAndCommentId(Long userId, Long commentId);

    /**
     * 删除点赞记录
     */
    void deleteByUserIdAndCommentId(Long userId, Long commentId);
}
