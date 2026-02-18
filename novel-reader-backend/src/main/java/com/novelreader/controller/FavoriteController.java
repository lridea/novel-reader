package com.novelreader.controller;

import com.novelreader.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 收藏控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping
    public Map<String, Object> addFavorite(@RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        Long novelId = Long.valueOf(request.get("novelId").toString());
        String note = (String) request.get("note");

        return favoriteService.addFavorite(userId, novelId, note);
    }

    /**
     * 取消收藏
     */
    @DeleteMapping("/{novelId}")
    public Map<String, Object> removeFavorite(@PathVariable Long novelId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.removeFavorite(userId, novelId);
    }

    /**
     * 获取收藏列表
     */
    @GetMapping
    public Map<String, Object> getFavoriteList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.getFavoriteList(userId, page, size);
    }

    /**
     * 更新收藏备注
     */
    @PutMapping("/{novelId}/note")
    public Map<String, Object> updateFavoriteNote(
            @PathVariable Long novelId,
            @RequestBody Map<String, String> request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        String note = request.get("note");

        return favoriteService.updateFavoriteNote(userId, novelId, note);
    }
}
