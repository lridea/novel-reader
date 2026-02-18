package com.novelreader.repository;

import com.novelreader.entity.CrawlerTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 爬虫任务 Repository
 */
@Repository
public interface CrawlerTaskRepository extends JpaRepository<CrawlerTask, Long> {

    /**
     * 根据平台分页查询任务
     */
    Page<CrawlerTask> findByPlatformOrderByStartTimeDesc(String platform, Pageable pageable);

    /**
     * 根据状态分页查询任务
     */
    Page<CrawlerTask> findByStatusOrderByStartTimeDesc(String status, Pageable pageable);

    /**
     * 根据平台和状态查询运行中的任务
     */
    CrawlerTask findFirstByPlatformAndStatusOrderByStartTimeDesc(String platform, String status);
}
