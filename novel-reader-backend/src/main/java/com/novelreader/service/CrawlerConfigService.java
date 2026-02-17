package com.novelreader.service;

import com.novelreader.entity.CrawlerConfig;
import com.novelreader.entity.Novel;
import com.novelreader.repository.CrawlerConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 爬虫配置服务
 */
@Slf4j
@Service
public class CrawlerConfigService {

    @Autowired
    private CrawlerConfigRepository crawlerConfigRepository;

    /**
     * 获取所有启用的配置
     */
    public List<CrawlerConfig> findAllEnabled() {
        return crawlerConfigRepository.findByEnabled(1);
    }

    /**
     * 获取所有配置
     */
    public List<CrawlerConfig> findAll() {
        return crawlerConfigRepository.findAll();
    }

    /**
     * 根据平台名称获取配置
     */
    public CrawlerConfig findByPlatform(String platform) {
        return crawlerConfigRepository.findByPlatform(platform);
    }

    public CrawlerConfig save(CrawlerConfig config) {
        return crawlerConfigRepository.save(config);
    }

    public void delete(Long id) {
        crawlerConfigRepository.deleteById(id);
    }
}
