package com.novelreader.repository;

import com.novelreader.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

/**
 * 小说Repository
 */
@Repository
public interface NovelRepository extends JpaRepository<Novel, Long> {

    /**
     * 根据平台和小说ID查找
     */
    Novel findByPlatformAndNovelId(String platform, String novelId);

    /**
     * 根据平台查找
     */
    List<Novel> findByPlatform(String platform);

    /**
     * 根据状态查找
     */
    List<Novel> findByStatus(Integer status);

    /**
     * 分页查询所有小说
     */
    Page<Novel> findByDeletedOrderByLatestUpdateTimeDesc(Integer deleted, Pageable pageable);

    /**
     * 根据平台分页查询小说
     */
    Page<Novel> findByPlatformAndDeletedOrderByLatestUpdateTimeDesc(String platform, Integer deleted, Pageable pageable);

    /**
     * 根据标题或作者搜索（分页）
     */
    @Query("SELECT n FROM Novel n WHERE n.deleted = 0 AND (n.title LIKE %:keyword% OR n.author LIKE %:keyword%)")
    Page<Novel> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * 根据平台和关键词搜索（分页）
     */
    @Query("SELECT n FROM Novel n WHERE n.platform = :platform AND n.deleted = 0 AND (n.title LIKE %:keyword% OR n.author LIKE %:keyword%)")
    Page<Novel> searchByPlatformAndKeyword(@Param("platform") String platform, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 复杂查询（支持多条件组合）
     * 支持平台、关键词、状态、标签、字数范围、收藏数范围、排序
     */
    @Query("SELECT n FROM Novel n WHERE n.deleted = 0 " +
           "AND (:platform IS NULL OR n.platform = :platform) " +
           "AND (:keyword IS NULL OR n.title LIKE %:keyword% OR n.author LIKE %:keyword%) " +
           "AND (:status IS NULL OR n.status = :status) " +
           "AND (:tag IS NULL OR n.tags LIKE %:tag%) " +
           "AND (:wordCountMin IS NULL OR n.wordCount >= :wordCountMin) " +
           "AND (:wordCountMax IS NULL OR n.wordCount <= :wordCountMax) " +
           "AND (:favoriteCountMin IS NULL OR n.favoriteCount >= :favoriteCountMin) " +
           "AND (:favoriteCountMax IS NULL OR n.favoriteCount <= :favoriteCountMax)")
    Page<Novel> searchNovels(
            @Param("platform") String platform,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("tag") String tag,
            @Param("wordCountMin") Long wordCountMin,
            @Param("wordCountMax") Long wordCountMax,
            @Param("favoriteCountMin") Integer favoriteCountMin,
            @Param("favoriteCountMax") Integer favoriteCountMax,
            Pageable pageable);

    /**
     * 获取所有标签（按数量降序，原生SQL）
     */
    @Query(value = "SELECT JSON_UNQUOTE(JSON_EXTRACT(tags, CONCAT('$[', seq.seq, ']'))) as tag_name, " +
                   "COUNT(*) as tag_count " +
                   "FROM t_novel, " +
                   "(SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) seq " +
                   "WHERE JSON_LENGTH(tags) > seq.seq " +
                   "AND deleted = 0 " +
                   "GROUP BY tag_name " +
                   "ORDER BY tag_count DESC " +
                   "LIMIT 100", nativeQuery = true)
    List<Map<String, Object>> getAllTags();

    /**
     * 根据平台获取标签（按数量降序，原生SQL）
     */
    @Query(value = "SELECT JSON_UNQUOTE(JSON_EXTRACT(tags, CONCAT('$[', seq.seq, ']'))) as tag_name, " +
                   "COUNT(*) as tag_count " +
                   "FROM t_novel, " +
                   "(SELECT 0 AS seq UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9) seq " +
                   "WHERE platform = :platform " +
                   "AND JSON_LENGTH(tags) > seq.seq " +
                   "AND deleted = 0 " +
                   "GROUP BY tag_name " +
                   "ORDER BY tag_count DESC " +
                   "LIMIT 100", nativeQuery = true)
    List<Map<String, Object>> getTagsByPlatform(@Param("platform") String platform);

    /**
     * 原子操作：收藏数+1
     */
    @Modifying
    @Query("UPDATE Novel n SET n.favoriteCount = n.favoriteCount + 1 WHERE n.id = :novelId")
    @Transactional
    void incrementFavoriteCount(@Param("novelId") Long novelId);

    /**
     * 原子操作：收藏数-1
     */
    @Modifying
    @Query("UPDATE Novel n SET n.favoriteCount = n.favoriteCount - 1 WHERE n.id = :novelId AND n.favoriteCount > 0")
    @Transactional
    void decrementFavoriteCount(@Param("novelId") Long novelId);

    /**
     * 原子操作：评论数+1
     */
    @Modifying
    @Query("UPDATE Novel n SET n.commentCount = n.commentCount + 1 WHERE n.id = :novelId")
    @Transactional
    void incrementCommentCount(@Param("novelId") Long novelId);

    /**
     * 原子操作：评论数-1
     */
    @Modifying
    @Query("UPDATE Novel n SET n.commentCount = n.commentCount - 1 WHERE n.id = :novelId AND n.commentCount > 0")
    @Transactional
    void decrementCommentCount(@Param("novelId") Long novelId);
}
