package com.novelreader.util;

import com.novelreader.entity.SensitiveWord;
import com.novelreader.repository.SensitiveWordRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 敏感词导入工具
 * 用于从文本文件导入敏感词库
 */
@Slf4j
@Component
public class SensitiveWordImporter {

    @Autowired
    private SensitiveWordRepository sensitiveWordRepository;

    /**
     * 从文本文件导入敏感词
     *
     * @param filePath 文件路径
     */
    public void importFromFile(String filePath) {
        log.info("开始导入敏感词库: {}", filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            String currentCategory = null;
            List<String> words = new ArrayList<>();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 跳过空行和注释
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                // 判断是否为分类标题（以##开头）
                if (line.startsWith("##")) {
                    // 先导入上一分类的敏感词
                    if (currentCategory != null && !words.isEmpty()) {
                        importWords(currentCategory, words);
                        words.clear();
                    }

                    // 获取新分类
                    currentCategory = line.substring(2).trim();
                    log.info("开始导入分类: {}", currentCategory);
                } else {
                    // 添加敏感词
                    words.add(line);
                }
            }

            // 导入最后一个分类的敏感词
            if (currentCategory != null && !words.isEmpty()) {
                importWords(currentCategory, words);
            }

            log.info("敏感词库导入完成");

        } catch (IOException e) {
            log.error("导入敏感词库失败", e);
        }
    }

    /**
     * 导入敏感词列表
     *
     * @param category 分类
     * @param words    敏感词列表
     */
    private void importWords(String category, List<String> words) {
        int successCount = 0;
        int skipCount = 0;

        for (String word : words) {
            word = word.trim();
            if (word.isEmpty()) {
                continue;
            }

            // 检查敏感词是否已存在
            if (sensitiveWordRepository.findByWord(word).isPresent()) {
                skipCount++;
                continue;
            }

            // 创建敏感词
            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(word);
            sensitiveWord.setCategory(category);
            sensitiveWord.setSeverity(1);
            sensitiveWord.setEnabled(1);

            sensitiveWordRepository.save(sensitiveWord);
            successCount++;
        }

        log.info("分类 {} 导入完成: 成功 {}, 跳过 {}", category, successCount, skipCount);
    }

    /**
     * 批量导入敏感词
     *
     * @param words 敏感词列表
     */
    public void importWords(List<String> words) {
        int successCount = 0;
        int skipCount = 0;

        for (String word : words) {
            word = word.trim();
            if (word.isEmpty()) {
                continue;
            }

            // 检查敏感词是否已存在
            if (sensitiveWordRepository.findByWord(word).isPresent()) {
                skipCount++;
                continue;
            }

            // 创建敏感词
            SensitiveWord sensitiveWord = new SensitiveWord();
            sensitiveWord.setWord(word);
            sensitiveWord.setCategory("其他");
            sensitiveWord.setSeverity(1);
            sensitiveWord.setEnabled(1);

            sensitiveWordRepository.save(sensitiveWord);
            successCount++;
        }

        log.info("批量导入完成: 成功 {}, 跳过 {}", successCount, skipCount);
    }
}
