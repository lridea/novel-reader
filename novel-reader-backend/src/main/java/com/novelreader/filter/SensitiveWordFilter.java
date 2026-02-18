package com.novelreader.filter;

import com.novelreader.entity.SensitiveWord;
import com.novelreader.filter.trie.TrieTree;
import com.novelreader.repository.SensitiveWordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 * 敏感词过滤器
 * 使用Trie树实现，模块独立，避免与评论模块耦合
 */
@Slf4j
@Component
public class SensitiveWordFilter {

    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    /**
     * Trie树
     */
    private TrieTree trieTree;

    /**
     * 初始化Trie树
     * 从数据库加载所有启用的敏感词
     */
    @PostConstruct
    public void init() {
        log.info("初始化敏感词过滤器...");
        trieTree = new TrieTree();

        // 从数据库加载所有启用的敏感词
        List<SensitiveWord> sensitiveWords = sensitiveWordRepository.findByEnabledOrderBySeverityDesc(1);
        List<String> words = sensitiveWords.stream()
                .map(SensitiveWord::getWord)
                .toList();

        // 批量插入Trie树
        trieTree.insertBatch(words);

        log.info("敏感词过滤器初始化完成，共加载 {} 个敏感词", words.size());
    }

    /**
     * 检查文本是否包含敏感词
     *
     * @param text 待检查的文本
     * @return true-包含敏感词，false-不包含敏感词
     */
    public boolean isSensitive(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return trieTree.contains(text);
    }

    /**
     * 过滤文本（替换敏感词为***）
     *
     * @param text 待过滤的文本
     * @return FilterResult 过滤结果
     */
    public FilterResult filter(String text) {
        if (text == null || text.isEmpty()) {
            return FilterResult.success(text);
        }

        // 检查是否包含敏感词
        if (!trieTree.contains(text)) {
            return FilterResult.success(text);
        }

        // 过滤文本
        String filteredText = trieTree.filter(text);

        // 获取敏感词列表
        List<String> sensitiveWords = trieTree.getSensitiveWords(text);

        log.info("检测到敏感词: {}", sensitiveWords);

        return FilterResult.fail(filteredText, sensitiveWords);
    }

    /**
     * 重新加载敏感词库
     * 当敏感词更新后，调用此方法重新加载
     */
    public void reload() {
        log.info("重新加载敏感词库...");
        init();
    }

    /**
     * 获取敏感词数量
     *
     * @return 敏感词数量
     */
    public int getSensitiveWordCount() {
        return sensitiveWordRepository.findByEnabledOrderBySeverityDesc(1).size();
    }
}
