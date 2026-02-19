package com.novelreader.service;

import com.novelreader.entity.Dislike;
import com.novelreader.entity.Novel;
import com.novelreader.repository.DislikeRepository;
import com.novelreader.repository.NovelRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 小说服务
 */
@Slf4j
@Service
public class NovelService {

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private DislikeRepository dislikeRepository;

    @Autowired
    private TagManageService tagManageService;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * 保存小说
     */
    public Novel save(Novel novel) {
        Novel savedNovel = novelRepository.save(novel);
        // 同步标签到标签表
        if (novel.getTags() != null && !novel.getTags().isEmpty()) {
            tagManageService.parseAndAddTags(novel.getTags(), "CRAWL");
        }
        return savedNovel;
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

    /**
     * 批量删除小说（软删除）
     */
    @Transactional
    public Map<String, Object> batchDelete(List<Long> ids) {
        log.info("批量删除小说: ids={}", ids);

        Map<String, Object> result = new HashMap<>();

        if (ids == null || ids.isEmpty()) {
            result.put("success", false);
            result.put("message", "参数错误");
            return result;
        }

        int deleteCount = 0;
        for (Long id : ids) {
            Novel novel = novelRepository.findById(id).orElse(null);
            if (novel != null && novel.getDeleted() == 0) {
                novel.setDeleted(1);
                novelRepository.save(novel);
                deleteCount++;
            }
        }

        result.put("success", true);
        result.put("message", "批量删除成功");
        result.put("deleteCount", deleteCount);

        return result;
    }

    /**
     * 点踩书籍
     */
    @Transactional
    public Map<String, Object> dislikeNovel(Long userId, Long novelId) {
        log.info("点踩书籍: userId={}, novelId={}", userId, novelId);

        Map<String, Object> result = new HashMap<>();

        // 检查书籍是否存在
        Optional<Novel> novelOpt = novelRepository.findById(novelId);
        if (novelOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "书籍不存在");
            return result;
        }

        Novel novel = novelOpt.get();

        // 检查是否已点踩
        boolean exists = dislikeRepository.existsByUserIdAndNovelId(userId, novelId);
        if (exists) {
            result.put("success", false);
            result.put("message", "已点踩该书籍");
            result.put("disliked", true);
            result.put("dislikeCount", novel.getDislikeCount());
            return result;
        }

        // 创建点踩记录
        Dislike dislike = new Dislike();
        dislike.setUserId(userId);
        dislike.setNovelId(novelId);
        dislikeRepository.save(dislike);

        // 更新书籍的点踩数（原子操作）
        novelRepository.incrementDislikeCount(novelId);

        // 使用原生查询获取最新点踩数（绕过Hibernate缓存）
        Integer latestDislikeCount = novelRepository.getDislikeCountById(novelId);

        result.put("success", true);
        result.put("message", "点踩成功");
        result.put("disliked", true);
        result.put("dislikeCount", latestDislikeCount != null ? latestDislikeCount : novel.getDislikeCount() + 1);

        return result;
    }

    /**
     * 取消点踩书籍
     */
    @Transactional
    public Map<String, Object> undislikeNovel(Long userId, Long novelId) {
        log.info("取消点踩书籍: userId={}, novelId={}", userId, novelId);

        Map<String, Object> result = new HashMap<>();

        // 检查书籍是否存在
        Optional<Novel> novelOpt = novelRepository.findById(novelId);
        if (novelOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "书籍不存在");
            return result;
        }

        Novel novel = novelOpt.get();

        // 检查是否已点踩
        boolean exists = dislikeRepository.existsByUserIdAndNovelId(userId, novelId);
        if (!exists) {
            result.put("success", false);
            result.put("message", "未点踩该书籍");
            result.put("disliked", false);
            result.put("dislikeCount", novel.getDislikeCount());
            return result;
        }

        // 删除点踩记录
        dislikeRepository.deleteByUserIdAndNovelId(userId, novelId);

        // 更新书籍的点踩数（原子操作）
        novelRepository.decrementDislikeCount(novelId);

        // 使用原生查询获取最新点踩数（绕过Hibernate缓存）
        Integer latestDislikeCount = novelRepository.getDislikeCountById(novelId);

        result.put("success", true);
        result.put("message", "取消点踩成功");
        result.put("disliked", false);
        result.put("dislikeCount", latestDislikeCount != null ? latestDislikeCount : Math.max(0, novel.getDislikeCount() - 1));

        return result;
    }

    /**
     * 搜索书籍（管理员）
     */
    public Map<String, Object> searchNovels(Integer page, Integer size, String keyword, Integer minDislikeCount) {
        log.info("搜索书籍: page={}, size={}, keyword={}, minDislikeCount={}", page, size, keyword, minDislikeCount);

        Map<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<Novel> novelPage;

        if (keyword != null && !keyword.isEmpty()) {
            if (minDislikeCount != null && minDislikeCount > 0) {
                // 根据关键词和最小点踩数搜索
                novelPage = novelRepository.searchByKeywordAndMinDislikeCount(keyword, minDislikeCount, pageable);
            } else {
                // 只根据关键词搜索
                novelPage = novelRepository.searchByKeyword(keyword, pageable);
            }
        } else if (minDislikeCount != null && minDislikeCount > 0) {
            // 只根据最小点踩数搜索
            novelPage = novelRepository.searchByMinDislikeCount(minDislikeCount, pageable);
        } else {
            // 查询所有
            novelPage = novelRepository.findByDeletedOrderByLatestUpdateTimeDesc(0, pageable);
        }

        result.put("success", true);
        result.put("content", novelPage.getContent());
        result.put("totalElements", novelPage.getTotalElements());
        result.put("totalPages", novelPage.getTotalPages());
        result.put("size", novelPage.getSize());
        result.put("number", novelPage.getNumber());

        return result;
    }
}
