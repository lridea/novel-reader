package com.novelreader.repository;

import com.novelreader.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 收藏 Repository
 */
@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

    /**
     * 根据用户ID和小说ID查找
     */
    Optional<Favorite> findByUserIdAndNovelId(Long userId, Long novelId);

    /**
     * 批量查询收藏状态（根据用户ID和小说ID列表）
     */
    List<Favorite> findByUserIdAndNovelIdIn(Long userId, List<Long> novelIds);

    /**
     * 根据用户ID分页查询收藏
     */
    Page<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 检查是否已收藏
     */
    boolean existsByUserIdAndNovelId(Long userId, Long novelId);

    /**
     * 删除收藏
     */
    void deleteByUserIdAndNovelId(Long userId, Long novelId);
}
