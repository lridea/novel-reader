package com.novelreader.service;

import com.novelreader.entity.Novel;
import com.novelreader.repository.NovelRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 小说服务
 */
@Slf4j
@Service
public class NovelService {

    @Autowired
    private NovelRepository novelRepository;

    /**
     * 保存小说
     */
    public Novel save(Novel novel) {
        return novelRepository.save(novel);
    }

    /**
     * 根据平台和小说ID查找小说
     */
    public Novel findByPlatformAndNovelId(String platform, String novelId) {
        return novelRepository.findByPlatformAndNovelId(platform, novelId);
    }

    /**
     * 获取所有小说
     */
    public List<Novel> findAll() {
        return novelRepository.findAll();
    }

    /**
     * 根据平台获取小说
     */
    public List<Novel> findByPlatform(String platform) {
        return novelRepository.findByPlatform(platform);
    }

    /**
     * 根据状态获取小说
     */
    public List<Novel> findByStatus(Integer status) {
        return novelRepository.findByStatus(status);
    }

    /**
     * 删除小说（软删除）
     */
    public void delete(Long id) {
        Novel novel = novelRepository.findById(id).orElse(null);
        if (novel != null) {
            novel.setDeleted(1);
            novelRepository.save(novel);
        }
    }
}
