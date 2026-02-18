package com.novelreader.repository;

import com.novelreader.entity.Dislike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 书籍点踩Repository
 */
@Repository
public interface DislikeRepository extends JpaRepository<Dislike, Long> {

    /**
     * 检查用户是否已点踩书籍
     */
    Optional<Dislike> findByUserIdAndNovelId(Long userId, Long novelId);

    /**
     * 检查用户是否已点踩书籍（是否存在）
     */
    boolean existsByUserIdAndNovelId(Long userId, Long novelId);

    /**
     * 删除点踩记录
     */
    void deleteByUserIdAndNovelId(Long userId, Long novelId);

    /**
     * 统计书籍的点踩数
     */
    long countByNovelId(Long novelId);
}
