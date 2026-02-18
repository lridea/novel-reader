package com.novelreader.service;

import com.novelreader.entity.Favorite;
import com.novelreader.entity.Novel;
import com.novelreader.repository.FavoriteRepository;
import com.novelreader.repository.NovelRepository;
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
import java.util.stream.Collectors;

/**
 * 收藏服务
 */
@Slf4j
@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private NovelRepository novelRepository;

    /**
     * 添加收藏
     */
    @Transactional
    public Map<String, Object> addFavorite(Long userId, Long novelId, String note) {
        log.info("添加收藏: userId={}, novelId={}", userId, novelId);

        Map<String, Object> result = new HashMap<>();

        // 检查小说是否存在
        Optional<Novel> novelOpt = novelRepository.findById(novelId);
        if (novelOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "小说不存在");
            return result;
        }

        Novel novel = novelOpt.get();

        // 检查是否已收藏
        if (favoriteRepository.existsByUserIdAndNovelId(userId, novelId)) {
            result.put("success", false);
            result.put("message", "已收藏该小说");
            return result;
        }

        // 创建收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setNovelId(novelId);
        favorite.setPlatform(novel.getPlatform());
        favorite.setPlatformNovelId(novel.getNovelId());
        favorite.setNote(note);
        favorite.setLatestChapterTitle(novel.getLatestChapterTitle());
        favorite.setLatestUpdateTime(novel.getLatestUpdateTime());
        favorite.setHasUpdate(0);

        Favorite savedFavorite = favoriteRepository.save(favorite);

        // 使用原子操作更新收藏数（解决并发问题）
        novelRepository.incrementFavoriteCount(novelId);

        // 重新获取最新的收藏数
        Novel updatedNovel = novelRepository.findById(novelId).orElse(null);

        result.put("success", true);
        result.put("message", "收藏成功");
        result.put("favorite", toFavoriteInfo(savedFavorite));
        result.put("novelFavoriteCount", updatedNovel != null ? updatedNovel.getFavoriteCount() : 0);

        return result;
    }

    /**
     * 取消收藏
     */
    @Transactional
    public Map<String, Object> removeFavorite(Long userId, Long novelId) {
        log.info("取消收藏: userId={}, novelId={}", userId, novelId);

        Map<String, Object> result = new HashMap<>();

        // 检查是否已收藏
        if (!favoriteRepository.existsByUserIdAndNovelId(userId, novelId)) {
            result.put("success", false);
            result.put("message", "未收藏该小说");
            return result;
        }

        // 使用原子操作更新收藏数（解决并发问题）
        novelRepository.decrementFavoriteCount(novelId);

        // 删除收藏
        favoriteRepository.deleteByUserIdAndNovelId(userId, novelId);

        // 重新获取最新的收藏数
        Novel updatedNovel = novelRepository.findById(novelId).orElse(null);

        result.put("success", true);
        result.put("message", "取消成功");
        result.put("novelFavoriteCount", updatedNovel != null ? updatedNovel.getFavoriteCount() : 0);

        return result;
    }

    /**
     * 获取收藏列表
     */
    public Map<String, Object> getFavoriteList(Long userId, int page, int size) {
        log.info("获取收藏列表: userId={}, page={}, size={}", userId, page, size);

        Map<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Favorite> favoritePage = favoriteRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);

        result.put("success", true);
        result.put("content", favoritePage.getContent().stream().map(this::toFavoriteInfo).toList());
        result.put("totalElements", favoritePage.getTotalElements());
        result.put("totalPages", favoritePage.getTotalPages());
        result.put("size", favoritePage.getSize());
        result.put("number", favoritePage.getNumber());

        return result;
    }

    /**
     * 更新收藏备注
     */
    @Transactional
    public Map<String, Object> updateFavoriteNote(Long userId, Long novelId, String note) {
        log.info("更新收藏备注: userId={}, novelId={}, note={}", userId, novelId, note);

        Map<String, Object> result = new HashMap<>();

        Optional<Favorite> favoriteOpt = favoriteRepository.findByUserIdAndNovelId(userId, novelId);
        if (favoriteOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "未收藏该小说");
            return result;
        }

        Favorite favorite = favoriteOpt.get();
        favorite.setNote(note);

        Favorite savedFavorite = favoriteRepository.save(favorite);

        result.put("success", true);
        result.put("message", "更新成功");
        result.put("favorite", toFavoriteInfo(savedFavorite));

        return result;
    }

    /**
     * 转换为收藏信息
     */
    private Map<String, Object> toFavoriteInfo(Favorite favorite) {
        Map<String, Object> favoriteInfo = new HashMap<>();
        favoriteInfo.put("id", favorite.getNovelId());
        favoriteInfo.put("novelId", favorite.getNovelId());
        favoriteInfo.put("platform", favorite.getPlatform());
        favoriteInfo.put("platformNovelId", favorite.getPlatformNovelId());
        favoriteInfo.put("note", favorite.getNote());
        favoriteInfo.put("latestChapterTitle", favorite.getLatestChapterTitle());
        favoriteInfo.put("latestUpdateTime", favorite.getLatestUpdateTime());
        favoriteInfo.put("hasUpdate", favorite.getHasUpdate());
        favoriteInfo.put("createdAt", favorite.getCreatedAt());
        favoriteInfo.put("updatedAt", favorite.getUpdatedAt());

        Optional<Novel> novelOpt = novelRepository.findById(favorite.getNovelId());
        if (novelOpt.isPresent()) {
            Novel novel = novelOpt.get();
            favoriteInfo.put("title", novel.getTitle());
            favoriteInfo.put("author", novel.getAuthor());
            favoriteInfo.put("coverUrl", novel.getCoverUrl());
            favoriteInfo.put("description", novel.getDescription());
            favoriteInfo.put("status", novel.getStatus());
        }

        return favoriteInfo;
    }

    /**
     * 批量查询收藏状态
     */
    public Map<String, Object> checkBatchFavorites(Long userId, String novelIds) {
        log.info("批量查询收藏状态: userId={}, novelIds={}", userId, novelIds);

        Map<String, Object> result = new HashMap<>();

        // 解析novelIds（逗号分隔）
        List<Long> novelIdList = List.of(novelIds.split(","))
                .stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 批量查询收藏状态
        List<Favorite> favorites = favoriteRepository.findByUserIdAndNovelIdIn(userId, novelIdList);

        // 转换为Map
        Map<String, Boolean> favoriteMap = favorites.stream()
                .collect(Collectors.toMap(
                        f -> f.getNovelId().toString(),
                        f -> true
                ));

        // 填充未收藏的小说
        for (Long novelId : novelIdList) {
            favoriteMap.putIfAbsent(novelId.toString(), false);
        }

        result.put("success", true);
        result.put("favorites", favoriteMap);

        return result;
    }
}
