package com.novelreader.controller;

import com.novelreader.service.NovelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 小说Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/novels")
public class NovelController {

    @Autowired
    private NovelService novelService;

    /**
     * 点踩书籍
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/dislike")
    public Map<String, Object> dislikeNovel(@PathVariable Long id) {
        log.info("点踩书籍: novelId={}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Long userId = (Long) authentication.getPrincipal();
        return novelService.dislikeNovel(userId, id);
    }

    /**
     * 取消点踩书籍
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/dislike")
    public Map<String, Object> undislikeNovel(@PathVariable Long id) {
        log.info("取消点踩书籍: novelId={}", id);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Long userId = (Long) authentication.getPrincipal();
        return novelService.undislikeNovel(userId, id);
    }

    /**
     * 批量删除书籍（软删除）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/batch")
    public Map<String, Object> batchDelete(@RequestBody Map<String, List<Long>> request) {
        log.info("批量删除书籍: {}", request);

        List<Long> ids = request.get("ids");
        return novelService.batchDelete(ids);
    }

    /**
     * 搜索书籍（管理员）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/search")
    public Map<String, Object> searchNovels(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer minDislikeCount
    ) {
        log.info("搜索书籍: page={}, size={}, keyword={}, minDislikeCount={}", page, size, keyword, minDislikeCount);

        return novelService.searchNovels(page, size, keyword, minDislikeCount);
    }
}
