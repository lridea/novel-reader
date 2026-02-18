package com.novelreader.controller;

import com.novelreader.service.TagService;
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
 * 标签Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/tags")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 添加标签（用户）
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user")
    public Map<String, Object> addTag(@RequestBody Map<String, Object> request) {
        log.info("添加标签: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Long userId = (Long) authentication.getPrincipal();
        Long novelId = Long.valueOf(request.get("novelId").toString());
        String tag = (String) request.get("tag");

        return tagService.addTag(userId, novelId, tag);
    }

    /**
     * 查询我的标签（用户）
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/my")
    public Map<String, Object> getMyTags(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status
    ) {
        log.info("查询我的标签: page={}, size={}, status={}", page, size, status);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        Long userId = (Long) authentication.getPrincipal();
        return tagService.getMyTags(userId, page, size, status);
    }

    /**
     * 查询待审核标签（管理员）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/audits")
    public Map<String, Object> getPendingAudits(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status
    ) {
        log.info("查询待审核标签: page={}, size={}, status={}", page, size, status);

        return tagService.getPendingAudits(page, size, status);
    }

    /**
     * 审核标签（管理员）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/audits/{id}")
    public Map<String, Object> auditTag(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request
    ) {
        log.info("审核标签: id={}, request={}", id, request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long reviewedBy = (Long) authentication.getPrincipal();
        Integer status = Integer.valueOf(request.get("status").toString());
        String reason = (String) request.get("reason");

        return tagService.auditTag(id, status, reason, reviewedBy);
    }

    /**
     * 批量审核标签（管理员）
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/audits/batch")
    public Map<String, Object> batchAuditTags(@RequestBody Map<String, Object> request) {
        log.info("批量审核标签: {}", request);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long reviewedBy = (Long) authentication.getPrincipal();
        List<Long> ids = (List<Long>) request.get("ids");
        Integer status = Integer.valueOf(request.get("status").toString());
        String reason = (String) request.get("reason");

        return tagService.batchAuditTags(ids, status, reason, reviewedBy);
    }

    /**
     * 获取所有用户标签
     */
    @GetMapping("/user-tags")
    public Map<String, Object> getAllUserTags() {
        log.info("获取所有用户标签");

        Map<String, Object> result = new HashMap<>();
        List<String> tags = tagService.getAllUserTags();

        result.put("success", true);
        result.put("tags", tags);

        return result;
    }
}
