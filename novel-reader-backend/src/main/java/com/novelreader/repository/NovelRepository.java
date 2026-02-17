package com.novelreader.repository;

import com.novelreader.entity.Novel;
import org.springframework.data.jpa.repository.JpaRepository;
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
}
