package com.novelreader.service;

import com.novelreader.entity.Tag;
import com.novelreader.repository.TagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 标签管理服务
 */
@Slf4j
@Service
public class TagManageService {

    @Autowired
    private TagRepository tagRepository;

    /**
     * 添加标签（如果不存在）
     * @param name 标签名称
     * @param source 来源：CRAWL-爬取, USER-用户添加
     */
    public void addTagIfNotExists(String name, String source) {
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        
        String trimmedName = name.trim();
        if (!tagRepository.existsByName(trimmedName)) {
            Tag tag = new Tag();
            tag.setName(trimmedName);
            tag.setSource(source);
            tagRepository.save(tag);
            log.info("新增标签: {} (来源: {})", trimmedName, source);
        }
    }

    /**
     * 批量添加标签（如果不存在）
     * @param names 标签名称列表
     * @param source 来源
     */
    public void addTagsIfNotExist(List<String> names, String source) {
        if (names == null || names.isEmpty()) {
            return;
        }
        for (String name : names) {
            addTagIfNotExists(name, source);
        }
    }

    /**
     * 从逗号分隔的字符串解析并添加标签
     * @param tagsStr 逗号分隔的标签字符串
     * @param source 来源
     */
    public void parseAndAddTags(String tagsStr, String source) {
        if (tagsStr == null || tagsStr.trim().isEmpty()) {
            return;
        }
        String[] tags = tagsStr.split(",");
        for (String tag : tags) {
            addTagIfNotExists(tag.trim(), source);
        }
    }

    /**
     * 获取所有标签
     */
    public Map<String, Object> getAllTags() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> tags = tagRepository.findAllTags();
        result.put("success", true);
        result.put("tags", tags);
        return result;
    }
}
