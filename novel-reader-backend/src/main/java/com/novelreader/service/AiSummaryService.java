package com.novelreader.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;

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

    @Value("${ai.zhipu.max-tokens:200}")
    private int maxTokens;

    @Autowired(required = false)
    private CacheService cacheService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    private static final String SYSTEM_PROMPT = "你是一个专业的小说内容概括助手。请将用户提供的小说章节内容概括为100-200字的简介，要求：1. 突出主要情节和人物；2. 语言简洁流畅；3. 不要剧透后续发展；4. 保持原作风格。";

    private static final String USER_PROMPT_TEMPLATE = "请概括以下小说章节内容：\n\n%s";

    public String summarize(String content) {
        log.info("开始生成AI概括，内容长度: {}", content.length());

        if (content == null || content.isEmpty()) {
            return "暂无内容";
        }

        String contentHash = md5Hash(content);
        
        if (cacheService != null) {
            String cachedSummary = cacheService.getAiSummary(contentHash);
            if (cachedSummary != null) {
                log.info("使用缓存的AI概括");
                return cachedSummary;
            }
        }

        String summary;
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("未配置智谱AI API Key，返回默认概括");
            summary = generateDefaultSummary(content);
        } else {
            try {
                String truncatedContent = truncateContent(content, 8000);
                summary = callZhipuApi(truncatedContent);
            } catch (Exception e) {
                log.error("调用智谱AI API失败: {}", e.getMessage(), e);
                summary = generateDefaultSummary(content);
            }
        }

        if (cacheService != null) {
            cacheService.cacheAiSummary(contentHash, summary);
        }

        return summary;
    }

    private String md5Hash(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(content.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return String.valueOf(content.hashCode());
        }
    }

    private String callZhipuApi(String content) throws Exception {
        String userPrompt = String.format(USER_PROMPT_TEMPLATE, content);

        String requestBody = objectMapper.writeValueAsString(new Object() {
            public String getModel() { return model; }
            public Object[] getMessages() {
                return new Object[] {
                    new Object() {
                        public String getRole() { return "system"; }
                        public String getContent() { return SYSTEM_PROMPT; }
                    },
                    new Object() {
                        public String getRole() { return "user"; }
                        public String getContent() { return userPrompt; }
                    }
                };
            }
            public int getMaxTokens() { return maxTokens; }
            public double getTemperature() { return 0.7; }
        });

        Request request = new Request.Builder()
                .url(baseUrl)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("智谱AI API请求失败，状态码: {}, 响应: {}", response.code(), errorBody);
                throw new RuntimeException("API请求失败: " + response.code());
            }

            String responseBody = response.body().string();
            return parseZhipuResponse(responseBody);
        }
    }

    private String parseZhipuResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        if (root.has("error")) {
            String errorMessage = root.get("error").get("message").asText();
            log.error("智谱AI返回错误: {}", errorMessage);
            throw new RuntimeException("智谱AI错误: " + errorMessage);
        }

        JsonNode choices = root.get("choices");
        if (choices == null || choices.isEmpty()) {
            log.error("智谱AI返回结果为空");
            throw new RuntimeException("智谱AI返回结果为空");
        }

        JsonNode message = choices.get(0).get("message");
        if (message == null) {
            log.error("智谱AI返回消息为空");
            throw new RuntimeException("智谱AI返回消息为空");
        }

        String summary = message.get("content").asText();
        log.info("AI概括生成成功，长度: {}", summary.length());

        return summary;
    }

    private String truncateContent(String content, int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }

    private String generateDefaultSummary(String content) {
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
