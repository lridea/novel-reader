package com.novelreader.repository;

import com.novelreader.entity.CrawlerConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 爬虫配置Repository
 */
@Repository
public interface CrawlerConfigRepository extends JpaRepository<CrawlerConfig, String> {

    /**
     * 查找所有启用的配置
     */
    List<CrawlerConfig> findByEnabled(Integer enabled);
}
