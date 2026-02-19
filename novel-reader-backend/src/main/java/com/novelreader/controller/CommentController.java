package com.novelreader.controller;

import com.novelreader.service.CommentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 评论Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    /**
     * 获取评论列表（分页）
     */
    @GetMapping("/novel/{novelId}")
    public Map<String, Object> getComments(
            @PathVariable Long novelId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) Long parentId,
            Authentication authentication
    ) {
        log.info("获取评论列表: novelId={}, page={}, size={}, floor={}, parentId={}", novelId, page, size, floor, parentId);

        Long userId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                userId = (Long) principal;
            }
        }

        return commentService.getComments(novelId, page, size, floor, parentId, userId);
    }

    /**
     * 添加评论
     */
    @PostMapping
    public Map<String, Object> addComment(
            @RequestBody Map<String, Object> request,
            Authentication authentication
    ) {
        log.info("添加评论: {}", request);

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Long)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        Long userId = (Long) principal;

        Long novelId = ((Number) request.get("novelId")).longValue();
        Long parentId = request.get("parentId") != null ? ((Number) request.get("parentId")).longValue() : null;
        Long replyToId = request.get("replyToId") != null ? ((Number) request.get("replyToId")).longValue() : null;
        Integer floor = request.get("floor") != null ? ((Number) request.get("floor")).intValue() : 1;
        String content = (String) request.get("content");

        return commentService.addComment(userId, novelId, parentId, replyToId, floor, content);
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{id}/like")
    public Map<String, Object> likeComment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        log.info("点赞评论: commentId={}", id);

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Long)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        Long userId = (Long) principal;

        return commentService.likeComment(userId, id);
    }

    /**
     * 取消点赞评论
     */
    @DeleteMapping("/{id}/like")
    public Map<String, Object> unlikeComment(
            @PathVariable Long id,
            Authentication authentication
    ) {
        log.info("取消点赞评论: commentId={}", id);

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Long)) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }
        Long userId = (Long) principal;

        return commentService.unlikeComment(userId, id);
    }
}
