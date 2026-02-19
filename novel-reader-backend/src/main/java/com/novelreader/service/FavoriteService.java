package com.novelreader.service;

import com.novelreader.entity.Favorite;
import com.novelreader.entity.FavoriteCategory;
import com.novelreader.entity.Novel;
import com.novelreader.repository.FavoriteCategoryRepository;
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

@Slf4j
@Service
public class FavoriteService {

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private FavoriteCategoryRepository favoriteCategoryRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Transactional
    public Map<String, Object> addFavorite(Long userId, Long novelId, Long categoryId, String note) {
        log.info("添加收藏: userId={}, novelId={}, categoryId={}", userId, novelId, categoryId);

        Map<String, Object> result = new HashMap<>();

        Optional<Novel> novelOpt = novelRepository.findById(novelId);
        if (novelOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "小说不存在");
            return result;
        }

        Novel novel = novelOpt.get();

        if (categoryId == null) {
            List<FavoriteCategory> categories = favoriteCategoryRepository.findByUserIdOrderBySortOrderAsc(userId);
            if (!categories.isEmpty()) {
                categoryId = categories.get(0).getId();
            }
        } else {
            Optional<FavoriteCategory> categoryOpt = favoriteCategoryRepository.findById(categoryId);
            if (categoryOpt.isEmpty() || !categoryOpt.get().getUserId().equals(userId)) {
                result.put("success", false);
                result.put("message", "收藏夹不存在");
                return result;
            }
        }

        if (favoriteRepository.existsByUserIdAndNovelIdAndCategoryId(userId, novelId, categoryId)) {
            result.put("success", false);
            result.put("message", "已收藏该小说到此收藏夹");
            return result;
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setNovelId(novelId);
        favorite.setCategoryId(categoryId);
        favorite.setPlatform(novel.getPlatform());
        favorite.setPlatformNovelId(novel.getNovelId());
        favorite.setNote(note);
        favorite.setLatestChapterTitle(novel.getLatestChapterTitle());
        favorite.setLatestUpdateTime(novel.getLatestUpdateTime());
        favorite.setHasUpdate(0);

        Favorite savedFavorite = favoriteRepository.save(favorite);

        novelRepository.incrementFavoriteCount(novelId);

        Novel updatedNovel = novelRepository.findById(novelId).orElse(null);

        result.put("success", true);
        result.put("message", "收藏成功");
        result.put("favorite", toFavoriteInfo(savedFavorite));
        result.put("novelFavoriteCount", updatedNovel != null ? updatedNovel.getFavoriteCount() : 0);

        return result;
    }

    @Transactional
    public Map<String, Object> removeFavorite(Long userId, Long novelId, Long categoryId) {
        log.info("取消收藏: userId={}, novelId={}, categoryId={}", userId, novelId, categoryId);

        Map<String, Object> result = new HashMap<>();

        if (categoryId == null) {
            List<Favorite> favorites = favoriteRepository.findByUserIdAndNovelIdIn(userId, List.of(novelId));
            if (favorites.isEmpty()) {
                result.put("success", false);
                result.put("message", "未收藏该小说");
                return result;
            }
            for (Favorite favorite : favorites) {
                favoriteRepository.delete(favorite);
                novelRepository.decrementFavoriteCount(novelId);
            }
        } else {
            if (!favoriteRepository.existsByUserIdAndNovelIdAndCategoryId(userId, novelId, categoryId)) {
                result.put("success", false);
                result.put("message", "未收藏该小说到此收藏夹");
                return result;
            }
            favoriteRepository.deleteByUserIdAndNovelIdAndCategoryId(userId, novelId, categoryId);
            novelRepository.decrementFavoriteCount(novelId);
        }

        Novel updatedNovel = novelRepository.findById(novelId).orElse(null);

        result.put("success", true);
        result.put("message", "取消成功");
        result.put("novelFavoriteCount", updatedNovel != null ? updatedNovel.getFavoriteCount() : 0);

        return result;
    }

    public Map<String, Object> getFavoriteList(Long userId, Long categoryId, int page, int size, String sortBy, String keyword) {
        log.info("获取收藏列表: userId={}, categoryId={}, page={}, size={}, sortBy={}, keyword={}", userId, categoryId, page, size, sortBy, keyword);

        Map<String, Object> result = new HashMap<>();

        Sort sort;
        if ("updateTime".equals(sortBy)) {
            sort = Sort.by(Sort.Direction.DESC, "latestUpdateTime");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Favorite> favoritePage;

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();

        if (categoryId != null) {
            if (hasKeyword) {
                favoritePage = favoriteRepository.findByUserIdAndCategoryIdAndNovelTitleContaining(userId, categoryId, keyword.trim(), pageable);
            } else {
                favoritePage = favoriteRepository.findByUserIdAndCategoryId(userId, categoryId, pageable);
            }
        } else {
            if (hasKeyword) {
                favoritePage = favoriteRepository.findByUserIdAndNovelTitleContaining(userId, keyword.trim(), pageable);
            } else {
                favoritePage = favoriteRepository.findByUserId(userId, pageable);
            }
        }

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
