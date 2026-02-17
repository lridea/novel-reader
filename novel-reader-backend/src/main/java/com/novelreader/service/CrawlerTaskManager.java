package com.novelreader.service;

import com.novelreader.entity.CrawlerConfig;
import com.novelreader.repository.CrawlerConfigRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class CrawlerTaskManager {

    @Autowired
    private CrawlerConfigRepository crawlerConfigRepository;

    private final Map<String, Object> platformLocks = new ConcurrentHashMap<>();

    public boolean tryAcquireLock(String platform) {
        synchronized (getLock(platform)) {
            CrawlerConfig config = crawlerConfigRepository.findByPlatform(platform);
            if (config == null) {
                log.warn("平台 {} 配置不存在", platform);
                return false;
            }

            if (config.getIsRunning() == 1) {
                log.info("平台 {} 已有任务在运行，taskId: {}", platform, config.getRunningTaskId());
                return false;
            }

            String taskId = UUID.randomUUID().toString();
            config.setIsRunning(1);
            config.setRunningTaskId(taskId);
            config.setLastCrawlTime(LocalDateTime.now());
            crawlerConfigRepository.save(config);

            log.info("平台 {} 获取锁成功，taskId: {}", platform, taskId);
            return true;
        }
    }

    @Transactional
    public void releaseLock(String platform, boolean success, String errorMessage) {
        synchronized (getLock(platform)) {
            CrawlerConfig config = crawlerConfigRepository.findByPlatform(platform);
            if (config == null) {
                log.warn("平台 {} 配置不存在", platform);
                return;
            }

            config.setIsRunning(0);
            config.setRunningTaskId(null);

            if (success) {
                config.setLastSuccessCrawlTime(LocalDateTime.now());
                config.setCrawlCount(config.getCrawlCount() + 1);
                config.setLastErrorMessage(null);
            } else {
                config.setFailCount(config.getFailCount() + 1);
                config.setLastErrorMessage(errorMessage);
            }

            crawlerConfigRepository.save(config);
            log.info("平台 {} 释放锁，成功: {}", platform, success);
        }
    }

    public boolean isRunning(String platform) {
        CrawlerConfig config = crawlerConfigRepository.findByPlatform(platform);
        return config != null && config.getIsRunning() == 1;
    }

    public LocalDateTime getLastSuccessCrawlTime(String platform) {
        CrawlerConfig config = crawlerConfigRepository.findByPlatform(platform);
        return config != null ? config.getLastSuccessCrawlTime() : null;
    }

    private Object getLock(String platform) {
        return platformLocks.computeIfAbsent(platform, k -> new Object());
    }
}
