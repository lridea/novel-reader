package com.novelreader.repository;

import com.novelreader.entity.Novel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}
