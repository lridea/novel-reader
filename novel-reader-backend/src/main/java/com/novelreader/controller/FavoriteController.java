package com.novelreader.controller;

import com.novelreader.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

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
        Long categoryId = request.get("categoryId") != null ? Long.valueOf(request.get("categoryId").toString()) : null;
        String note = (String) request.get("note");

        return favoriteService.addFavorite(userId, novelId, categoryId, note);
    }

    @DeleteMapping("/{novelId}")
    public Map<String, Object> removeFavorite(
            @PathVariable Long novelId,
            @RequestParam(required = false) Long categoryId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.removeFavorite(userId, novelId, categoryId);
    }

    @DeleteMapping("/batch")
    public Map<String, Object> batchRemoveFavorites(@RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        java.util.List<Integer> novelIds = (java.util.List<Integer>) request.get("novelIds");
        
        if (novelIds == null || novelIds.isEmpty()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "请选择要取消收藏的书籍");
            return error;
        }

        return favoriteService.batchRemoveFavorites(userId, novelIds);
    }

    @GetMapping
    public Map<String, Object> getFavoriteList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String keyword) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        return favoriteService.getFavoriteList(userId, categoryId, page, size, sortBy, keyword);
    }

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

    @GetMapping("/check-batch")
    public Map<String, Object> checkBatchFavorites(@RequestParam String novelIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("favorites", new java.util.HashMap<>());
            return result;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Long)) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("success", true);
            result.put("favorites", new java.util.HashMap<>());
            return result;
        }

        Long userId = (Long) principal;
        return favoriteService.checkBatchFavorites(userId, novelIds);
    }
}
