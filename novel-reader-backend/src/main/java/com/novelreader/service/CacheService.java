package com.novelreader.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 缓存服务
 */
@Slf4j
@Service
public class CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String NOVEL_CACHE_PREFIX = "novel:";
    private static final String AI_SUMMARY_CACHE_PREFIX = "ai:summary:";
    private static final String CRAWLER_STATUS_PREFIX = "crawler:status:";

    private static final long DEFAULT_CACHE_TTL = 3600;
    private static final long AI_SUMMARY_CACHE_TTL = 86400 * 7;

    public void cacheNovel(String novelId, Object novel) {
        String key = NOVEL_CACHE_PREFIX + novelId;
        redisTemplate.opsForValue().set(key, novel, DEFAULT_CACHE_TTL, TimeUnit.SECONDS);
        log.debug("缓存小说: {}", novelId);
    }

    public Object getNovel(String novelId) {
        String key = NOVEL_CACHE_PREFIX + novelId;
        return redisTemplate.opsForValue().get(key);
    }

    public void deleteNovel(String novelId) {
        String key = NOVEL_CACHE_PREFIX + novelId;
        redisTemplate.delete(key);
        log.debug("删除小说缓存: {}", novelId);
    }

    public void cacheAiSummary(String contentHash, String summary) {
        String key = AI_SUMMARY_CACHE_PREFIX + contentHash;
        redisTemplate.opsForValue().set(key, summary, AI_SUMMARY_CACHE_TTL, TimeUnit.SECONDS);
        log.debug("缓存AI概括: {}", contentHash);
    }

    public String getAiSummary(String contentHash) {
        String key = AI_SUMMARY_CACHE_PREFIX + contentHash;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public void setCrawlerStatus(String platform, String status) {
        String key = CRAWLER_STATUS_PREFIX + platform;
        redisTemplate.opsForValue().set(key, status, 3600, TimeUnit.SECONDS);
    }

    public String getCrawlerStatus(String platform) {
        String key = CRAWLER_STATUS_PREFIX + platform;
        Object value = redisTemplate.opsForValue().get(key);
        return value != null ? value.toString() : null;
    }

    public void set(String key, Object value, long ttl) {
        redisTemplate.opsForValue().set(key, value, ttl, TimeUnit.SECONDS);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
