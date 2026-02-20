package com.novelreader.controller;

import com.novelreader.entity.Novel;
import com.novelreader.entity.TagAudit;
import com.novelreader.repository.DislikeRepository;
import com.novelreader.repository.NovelRepository;
import com.novelreader.repository.TagAuditRepository;
import com.novelreader.service.NovelService;
import com.novelreader.service.TagManageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/novels")
public class NovelController {

    @Autowired
    private NovelService novelService;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private TagManageService tagManageService;

    @Autowired
    private DislikeRepository dislikeRepository;

    @Autowired
    private TagAuditRepository tagAuditRepository;

    @GetMapping("/page")
    public Map<String, Object> getNovelsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String platforms,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Long wordCountMin,
            @RequestParam(required = false) Long wordCountMax,
            @RequestParam(required = false) Integer favoriteCountMin,
            @RequestParam(defaultValue = "updateTime") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {

        log.info("公开分页查询小说: page={}, size={}, platforms={}, keyword={}, tag={}, status={}, wordCountMin={}, wordCountMax={}, favoriteCountMin={}, sortBy={}, sortOrder={}",
                page, size, platforms, keyword, tag, status, wordCountMin, wordCountMax, favoriteCountMin, sortBy, sortOrder);

        Map<String, Object> result = new HashMap<>();

        Sort sort;
        if ("wordCount".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC, "wordCount");
        } else if ("createdAt".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC, "createdAt");
        } else if ("favoriteCount".equalsIgnoreCase(sortBy)) {
            sort = Sort.by("asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC, "favoriteCount");
        } else {
            sort = Sort.by("asc".equalsIgnoreCase(sortOrder) ? Sort.Direction.ASC : Sort.Direction.DESC, "latestUpdateTime");
        }

        // 解析平台列表（逗号分隔）
        List<String> platformList = null;
        if (platforms != null && !platforms.isEmpty()) {
            platformList = Arrays.asList(platforms.split(","));
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Novel> novelPage;
        
        // 根据是否有平台列表选择查询方法
        if (platformList != null && !platformList.isEmpty()) {
            novelPage = novelRepository.searchNovelsByPlatforms(
                    platformList,
                    keyword,
                    status,
                    tag,
                    wordCountMin,
                    wordCountMax,
                    favoriteCountMin,
                    null,
                    pageable
            );
        } else {
            novelPage = novelRepository.searchNovels(
                    null,
                    keyword,
                    status,
                    tag,
                    wordCountMin,
                    wordCountMax,
                    favoriteCountMin,
                    null,
                    pageable
            );
        }

        result.put("content", novelPage.getContent());
        result.put("totalElements", novelPage.getTotalElements());
        result.put("totalPages", novelPage.getTotalPages());
        result.put("size", novelPage.getSize());
        result.put("number", novelPage.getNumber());

        return result;
    }

    @GetMapping("/{id}")
    public Map<String, Object> getNovelById(@PathVariable Long id) {
        log.info("获取小说详情: id={}", id);

        Map<String, Object> result = new HashMap<>();
        Novel novel = novelRepository.findById(id).orElse(null);

        if (novel == null || novel.getDeleted() == 1) {
            result.put("success", false);
            result.put("message", "小说不存在");
            return result;
        }

        // 检查当前用户是否已点踩
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isDisliked = false;
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            Long userId = (Long) authentication.getPrincipal();
            isDisliked = dislikeRepository.existsByUserIdAndNovelId(userId, id);
        }

        // 获取已审核通过的用户标签
        List<TagAudit> approvedTags = tagAuditRepository.findByNovelIdAndStatus(id, 1);
        List<String> userTagList = approvedTags.stream()
                .map(TagAudit::getTag)
                .collect(Collectors.toList());
        
        log.info("获取小说详情: novelId={}, userTags={}", id, userTagList);

        // 清空 novel 中的 userTags，避免重复
        novel.setUserTags(null);

        result.put("success", true);
        result.put("data", novel);
        result.put("isDisliked", isDisliked);
        result.put("userTags", userTagList);
        return result;
    }

    @GetMapping("/tags")
    public Map<String, Object> getTags() {
        log.info("获取所有标签");
        return tagManageService.getAllTags();
    }

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
    public Map<String, Object> batchDelete(@RequestBody Map<String, Object> request) {
        log.info("批量删除书籍: {}", request);

        List<?> rawIds = (List<?>) request.get("ids");
        List<Long> ids = rawIds.stream()
                .map(id -> {
                    if (id instanceof Integer) {
                        return ((Integer) id).longValue();
                    } else if (id instanceof Long) {
                        return (Long) id;
                    }
                    return Long.parseLong(id.toString());
                })
                .collect(java.util.stream.Collectors.toList());
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
