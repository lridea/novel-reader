package com.novelreader.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.novelreader.entity.Novel;
import com.novelreader.entity.TagAudit;
import com.novelreader.entity.UserTag;
import com.novelreader.repository.NovelRepository;
import com.novelreader.repository.TagAuditRepository;
import com.novelreader.repository.UserTagRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * 标签Service
 */
@Slf4j
@Service
public class TagService {

    @Autowired
    private TagAuditRepository tagAuditRepository;

    @Autowired
    private UserTagRepository userTagRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 添加标签（用户）
     */
    @Transactional
    public Map<String, Object> addTag(Long userId, Long novelId, String tag) {
        log.info("添加标签: userId={}, novelId={}, tag={}", userId, novelId, tag);

        Map<String, Object> result = new HashMap<>();

        // 检查书籍是否存在
        Optional<Novel> novelOpt = novelRepository.findById(novelId);
        if (novelOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "书籍不存在");
            return result;
        }

        // 检查是否已添加相同标签
        boolean exists = tagAuditRepository.existsByUserIdAndNovelIdAndTag(userId, novelId, tag);
        if (exists) {
            result.put("success", false);
            result.put("message", "已添加该标签，请勿重复添加");
            return result;
        }

        // 创建标签审核记录
        TagAudit tagAudit = new TagAudit();
        tagAudit.setUserId(userId);
        tagAudit.setNovelId(novelId);
        tagAudit.setTag(tag);
        tagAudit.setStatus(0); // 待审核
        tagAuditRepository.save(tagAudit);

        result.put("success", true);
        result.put("message", "标签提交成功，等待审核");
        return result;
    }

    /**
     * 查询我的标签（用户）
     */
    public Map<String, Object> getMyTags(Long userId, Integer page, Integer size, Integer status) {
        log.info("查询我的标签: userId={}, page={}, size={}, status={}", userId, page, size, status);

        Map<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TagAudit> tagAuditPage;

        if (status != null) {
            tagAuditPage = tagAuditRepository.findByUserIdAndStatusOrderByCreatedAtDesc(userId, status, pageable);
        } else {
            tagAuditPage = tagAuditRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
        }

        // 转换为DTO
        List<Map<String, Object>> tagDtos = new ArrayList<>();
        for (TagAudit tagAudit : tagAuditPage.getContent()) {
            Map<String, Object> tagDto = new HashMap<>();
            tagDto.put("id", tagAudit.getId());
            tagDto.put("novelId", tagAudit.getNovelId());
            tagDto.put("tag", tagAudit.getTag());
            tagDto.put("status", tagAudit.getStatus());
            tagDto.put("createdAt", tagAudit.getCreatedAt());

            // 获取书名
            Optional<Novel> novelOpt = novelRepository.findById(tagAudit.getNovelId());
            novelOpt.ifPresent(novel -> tagDto.put("novelTitle", novel.getTitle()));

            tagDtos.add(tagDto);
        }

        result.put("success", true);
        result.put("content", tagDtos);
        result.put("totalElements", tagAuditPage.getTotalElements());
        result.put("totalPages", tagAuditPage.getTotalPages());
        result.put("size", tagAuditPage.getSize());
        result.put("number", tagAuditPage.getNumber());

        return result;
    }

    /**
     * 查询待审核标签（管理员）
     */
    public Map<String, Object> getPendingAudits(Integer page, Integer size, Integer status) {
        log.info("查询待审核标签: page={}, size={}, status={}", page, size, status);

        Map<String, Object> result = new HashMap<>();

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<TagAudit> tagAuditPage;

        if (status != null) {
            tagAuditPage = tagAuditRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        } else {
            tagAuditPage = tagAuditRepository.findByStatusOrderByCreatedAtDesc(0, pageable);
        }

        // 转换为DTO
        List<Map<String, Object>> tagDtos = new ArrayList<>();
        for (TagAudit tagAudit : tagAuditPage.getContent()) {
            Map<String, Object> tagDto = new HashMap<>();
            tagDto.put("id", tagAudit.getId());
            tagDto.put("novelId", tagAudit.getNovelId());
            tagDto.put("userId", tagAudit.getUserId());
            tagDto.put("tag", tagAudit.getTag());
            tagDto.put("status", tagAudit.getStatus());
            tagDto.put("createdAt", tagAudit.getCreatedAt());
            tagDto.put("reviewedBy", tagAudit.getReviewedBy());
            tagDto.put("reviewedAt", tagAudit.getReviewedAt());
            tagDto.put("reason", tagAudit.getReason());

            // 获取书名
            Optional<Novel> novelOpt = novelRepository.findById(tagAudit.getNovelId());
            novelOpt.ifPresent(novel -> tagDto.put("novelTitle", novel.getTitle()));

            tagDtos.add(tagDto);
        }

        result.put("success", true);
        result.put("content", tagDtos);
        result.put("totalElements", tagAuditPage.getTotalElements());
        result.put("totalPages", tagAuditPage.getTotalPages());
        result.put("size", tagAuditPage.getSize());
        result.put("number", tagAuditPage.getNumber());

        return result;
    }

    /**
     * 审核标签（管理员）
     */
    @Transactional
    public Map<String, Object> auditTag(Long auditId, Integer status, String reason, Long reviewedBy) {
        log.info("审核标签: auditId={}, status={}, reason={}, reviewedBy={}", auditId, status, reason, reviewedBy);

        Map<String, Object> result = new HashMap<>();

        // 检查审核记录是否存在
        Optional<TagAudit> tagAuditOpt = tagAuditRepository.findById(auditId);
        if (tagAuditOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "审核记录不存在");
            return result;
        }

        TagAudit tagAudit = tagAuditOpt.get();

        // 检查是否已审核
        if (tagAudit.getStatus() != 0) {
            result.put("success", false);
            result.put("message", "该标签已审核");
            return result;
        }

        // 更新审核状态
        tagAudit.setStatus(status);
        tagAudit.setReviewedBy(reviewedBy);
        tagAudit.setReviewedAt(LocalDateTime.now());
        tagAudit.setReason(reason);
        tagAuditRepository.save(tagAudit);

        // 如果审核通过，创建用户标签记录
        if (status == 1) {
            UserTag userTag = new UserTag();
            userTag.setUserId(tagAudit.getUserId());
            userTag.setNovelId(tagAudit.getNovelId());
            userTag.setTag(tagAudit.getTag());
            userTagRepository.save(userTag);

            // 更新书籍的userTags字段
            updateUserTags(tagAudit.getNovelId());
        }

        result.put("success", true);
        result.put("message", "审核成功");
        return result;
    }

    /**
     * 批量审核标签（管理员）
     */
    @Transactional
    public Map<String, Object> batchAuditTags(List<Long> auditIds, Integer status, String reason, Long reviewedBy) {
        log.info("批量审核标签: auditIds={}, status={}, reason={}, reviewedBy={}", auditIds, status, reason, reviewedBy);

        Map<String, Object> result = new HashMap<>();

        if (auditIds == null || auditIds.isEmpty()) {
            result.put("success", false);
            result.put("message", "参数错误");
            return result;
        }

        int approveCount = 0;
        int rejectCount = 0;

        // 使用Set去重，避免重复更新
        Set<Long> updatedNovelIds = new HashSet<>();

        for (Long auditId : auditIds) {
            Optional<TagAudit> tagAuditOpt = tagAuditRepository.findById(auditId);
            if (tagAuditOpt.isEmpty()) {
                continue;
            }

            TagAudit tagAudit = tagAuditOpt.get();

            // 跳过已审核的标签
            if (tagAudit.getStatus() != 0) {
                continue;
            }

            // 更新审核状态
            tagAudit.setStatus(status);
            tagAudit.setReviewedBy(reviewedBy);
            tagAudit.setReviewedAt(LocalDateTime.now());
            tagAudit.setReason(reason);
            tagAuditRepository.save(tagAudit);

            // 如果审核通过，创建用户标签记录
            if (status == 1) {
                UserTag userTag = new UserTag();
                userTag.setUserId(tagAudit.getUserId());
                userTag.setNovelId(tagAudit.getNovelId());
                userTag.setTag(tagAudit.getTag());
                userTagRepository.save(userTag);

                // 记录需要更新的书籍ID
                updatedNovelIds.add(tagAudit.getNovelId());
                approveCount++;
            } else {
                rejectCount++;
            }
        }

        // 批量更新书籍的userTags字段
        for (Long novelId : updatedNovelIds) {
            updateUserTags(novelId);
        }

        result.put("success", true);
        result.put("message", "批量审核成功");
        result.put("approveCount", approveCount);
        result.put("rejectCount", rejectCount);

        return result;
    }

    /**
     * 更新书籍的userTags字段
     */
    private void updateUserTags(Long novelId) {
        try {
            // 查询书籍的所有用户标签
            List<String> tags = userTagRepository.findTagsByNovelId(novelId);

            // 更新书籍的userTags字段
            Optional<Novel> novelOpt = novelRepository.findById(novelId);
            if (novelOpt.isPresent()) {
                Novel novel = novelOpt.get();
                String userTagsJson = objectMapper.writeValueAsString(tags);
                novel.setUserTags(userTagsJson);
                novelRepository.save(novel);
            }
        } catch (Exception e) {
            log.error("更新书籍userTags字段失败: novelId={}, error={}", novelId, e.getMessage(), e);
        }
    }

    /**
     * 获取所有用户标签
     */
    public List<String> getAllUserTags() {
        return userTagRepository.findAllTags();
    }

    /**
     * 删除标签（用户）
     */
    @Transactional
    public Map<String, Object> deleteTag(Long userId, Long novelId, String tag) {
        log.info("删除标签: userId={}, novelId={}, tag={}", userId, novelId, tag);

        Map<String, Object> result = new HashMap<>();

        // 检查标签是否存在
        Optional<UserTag> userTagOpt = userTagRepository.findByUserIdAndNovelIdAndTag(userId, novelId, tag);
        if (userTagOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "标签不存在");
            return result;
        }

        // 删除用户标签
        userTagRepository.deleteByUserIdAndNovelIdAndTag(userId, novelId, tag);

        // 更新书籍的userTags字段
        updateUserTags(novelId);

        result.put("success", true);
        result.put("message", "删除成功");
        return result;
    }
}
