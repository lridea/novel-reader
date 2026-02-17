package com.novelreader.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * AI概括服务
 * 调用智谱AI生成概括
 */
@Slf4j
@Service
public class AiSummaryService {

    @Value("${ai.zhipu.api-key:}")
    private String apiKey;

    @Value("${ai.zhipu.base-url:https://open.bigmodel.cn/api/paas/v4/chat/completions}")
    private String baseUrl;

    @Value("${ai.zhipu.model:glm-4}")
    private String model;

    @Value("${ai.zhipu.timeout:60000}")
    private int timeout;

    /**
     * 生成概括
     *
     * @param content 原始内容
     * @return 概括
     */
    public String summarize(String content) {
        log.info("开始生成AI概括，内容长度: {}", content.length());

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("未配置智谱AI API Key，返回默认概括");
            return generateDefaultSummary(content);
        }

        try {
            // TODO: 调用智谱AI API
            // 暂时返回默认概括
            return generateDefaultSummary(content);
        } catch (Exception e) {
            log.error("生成AI概括失败: {}", e.getMessage());
            return generateDefaultSummary(content);
        }
    }

    /**
     * 生成默认概括（简化版）
     */
    private String generateDefaultSummary(String content) {
        // 简化处理：截取前500字作为概括
        if (content == null || content.isEmpty()) {
            return "暂无内容";
        }

        int maxLength = 500;
        String summary = content.length() > maxLength ?
            content.substring(0, maxLength) + "..." :
            content;

        log.info("生成默认概括，长度: {}", summary.length());
        return summary;
    }
}
