package com.novelreader.service;

import com.novelreader.entity.Comment;
import com.novelreader.entity.CommentLike;
import com.novelreader.entity.Novel;
import com.novelreader.filter.FilterResult;
import com.novelreader.filter.SensitiveWordFilter;
import com.novelreader.repository.CommentLikeRepository;
import com.novelreader.repository.CommentRepository;
import com.novelreader.repository.NovelRepository;
import com.novelreader.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论Service
 */
@Slf4j
@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private NovelRepository novelRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SensitiveWordFilter sensitiveWordFilter;

    /**
     * 获取评论列表（分页）
     * 按点赞数降序展示
     */
    public Map<String, Object> getComments(Long novelId, Integer page, Integer size, Integer floor, Long parentId, Long userId) {
        log.info("获取评论列表: novelId={}, page={}, size={}, floor={}, parentId={}, userId={}", novelId, page, size, floor, parentId, userId);

        Map<String, Object> result = new HashMap<>();

        // 检查小说是否存在
        Optional<Novel> novelOpt = novelRepository.findById(novelId);
        if (novelOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "小说不存在");
            return result;
        }

        // 分页查询评论（按点赞数降序）
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "likeCount"));
        Page<Comment> commentPage;

        if (floor != null && floor == 2 && parentId != null) {
            // 查询回复（按时间升序）
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));
            commentPage = commentRepository.findByParentIdAndDeletedOrderByCreatedAtAsc(parentId, 0, pageable);
        } else {
            // 查询顶层评论（按点赞数降序）
            commentPage = commentRepository.findByNovelIdAndFloorAndDeletedOrderByLikeCountDesc(novelId, 1, 0, pageable);
        }

        // 转换为DTO
        List<Map<String, Object>> commentDtos = commentPage.getContent().stream()
                .map(comment -> toCommentInfo(comment, userId))
                .collect(Collectors.toList());

        result.put("success", true);
        result.put("content", commentDtos);
        result.put("totalElements", commentPage.getTotalElements());
        result.put("totalPages", commentPage.getTotalPages());
        result.put("size", commentPage.getSize());
        result.put("number", commentPage.getNumber());

        return result;
    }

    /**
     * 添加评论
     */
    @Transactional
    public Map<String, Object> addComment(Long userId, Long novelId, Long parentId, Integer floor, String content) {
        log.info("添加评论: userId={}, novelId={}, parentId={}, floor={}, content={}", userId, novelId, parentId, floor, content);

        Map<String, Object> result = new HashMap<>();

        // 检查小说是否存在
        Optional<Novel> novelOpt = novelRepository.findById(novelId);
        if (novelOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "小说不存在");
            return result;
        }

        Novel novel = novelOpt.get();

        // 如果是回复，检查父评论是否存在
        Comment parentComment = null;
        if (floor != null && floor == 2 && parentId != null) {
            Optional<Comment> parentOpt = commentRepository.findById(parentId);
            if (parentOpt.isEmpty()) {
                result.put("success", false);
                result.put("message", "父评论不存在");
                return result;
            }
            parentComment = parentOpt.get();
        }

        // 敏感词过滤
        FilterResult filterResult = sensitiveWordFilter.filter(content);
        if (filterResult.isSensitive()) {
            log.info("评论包含敏感词: {}", filterResult.getSensitiveWords());
            result.put("success", false);
            result.put("message", "评论包含敏感词，无法发布");
            return result;
        }

        // 创建评论
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setNovelId(novelId);
        comment.setParentId(parentId);
        comment.setFloor(floor != null ? floor : 1);
        comment.setContent(content);
        comment = commentRepository.save(comment);

        // 更新小说的评论数（原子操作）
        novelRepository.incrementCommentCount(novelId);

        // 如果是回复，更新父评论的回复数（原子操作）
        if (floor != null && floor == 2 && parentComment != null) {
            commentRepository.incrementReplyCount(parentId);
        }

        // 重新获取最新的小说评论数
        Novel updatedNovel = novelRepository.findById(novelId).orElse(null);

        result.put("success", true);
        result.put("message", "评论成功");
        result.put("comment", toCommentInfo(comment, userId));
        result.put("novelCommentCount", updatedNovel != null ? updatedNovel.getCommentCount() : 0);

        return result;
    }

    /**
     * 转换为评论信息
     */
    private Map<String, Object> toCommentInfo(Comment comment, Long userId) {
        Map<String, Object> commentInfo = new HashMap<>();

        // 基本信息
        commentInfo.put("id", comment.getId());
        commentInfo.put("userId", comment.getUserId());
        commentInfo.put("novelId", comment.getNovelId());
        commentInfo.put("parentId", comment.getParentId());
        commentInfo.put("floor", comment.getFloor());
        commentInfo.put("content", comment.getContent());
        commentInfo.put("likeCount", comment.getLikeCount());
        commentInfo.put("replyCount", comment.getReplyCount());
        commentInfo.put("createdAt", comment.getCreatedAt());

        // 如果是顶层评论，初始化replies为空数组
        if (comment.getFloor() != null && comment.getFloor() == 1) {
            commentInfo.put("replies", new ArrayList<>());
        }

        // 获取用户信息
        Optional<com.novelreader.entity.User> userOpt = userRepository.findById(comment.getUserId());
        if (userOpt.isPresent()) {
            com.novelreader.entity.User user = userOpt.get();
            Comment.UserInfo userInfo = new Comment.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatar(user.getAvatarUrl());
            commentInfo.put("user", userInfo);
        }

        // 如果是回复，获取父评论用户信息
        if (comment.getParentId() != null) {
            Optional<Comment> parentOpt = commentRepository.findById(comment.getParentId());
            if (parentOpt.isPresent()) {
                Comment parent = parentOpt.get();
                Optional<com.novelreader.entity.User> parentUserOpt = userRepository.findById(parent.getUserId());
                if (parentUserOpt.isPresent()) {
                    com.novelreader.entity.User parentUser = parentUserOpt.get();
                    Comment.UserInfo parentUserInfo = new Comment.UserInfo();
                    parentUserInfo.setId(parentUser.getId());
                    parentUserInfo.setUsername(parentUser.getUsername());
                    parentUserInfo.setNickname(parentUser.getNickname());
                    parentUserInfo.setAvatar(parentUser.getAvatarUrl());
                    commentInfo.put("parentUser", parentUserInfo);
                }
            }
        }

        // 当前用户是否已点赞
        if (userId != null) {
            boolean liked = commentLikeRepository.existsByUserIdAndCommentId(userId, comment.getId());
            commentInfo.put("liked", liked);
        } else {
            commentInfo.put("liked", false);
        }

        // 当前用户是否为评论作者
        if (userId != null) {
            commentInfo.put("isOwner", comment.getUserId().equals(userId));
        } else {
            commentInfo.put("isOwner", false);
        }

        return commentInfo;
    }

    /**
     * 点赞评论
     */
    @Transactional
    public Map<String, Object> likeComment(Long userId, Long commentId) {
        log.info("点赞评论: userId={}, commentId={}", userId, commentId);

        Map<String, Object> result = new HashMap<>();

        // 检查评论是否存在
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "评论不存在");
            return result;
        }

        Comment comment = commentOpt.get();

        // 检查是否已点赞
        boolean exists = commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
        if (exists) {
            result.put("success", false);
            result.put("message", "已点赞该评论");
            result.put("liked", true);
            result.put("commentLikeCount", comment.getLikeCount());
            return result;
        }

        // 创建点赞记录
        CommentLike commentLike = new CommentLike();
        commentLike.setUserId(userId);
        commentLike.setCommentId(commentId);
        commentLikeRepository.save(commentLike);

        // 更新评论的点赞数（原子操作）
        commentRepository.incrementLikeCount(commentId);

        // 重新获取最新的点赞数
        Comment updatedComment = commentRepository.findById(commentId).orElse(null);

        result.put("success", true);
        result.put("message", "点赞成功");
        result.put("liked", true);
        result.put("commentLikeCount", updatedComment != null ? updatedComment.getLikeCount() : 0);

        return result;
    }

    /**
     * 取消点赞评论
     */
    @Transactional
    public Map<String, Object> unlikeComment(Long userId, Long commentId) {
        log.info("取消点赞评论: userId={}, commentId={}", userId, commentId);

        Map<String, Object> result = new HashMap<>();

        // 检查评论是否存在
        Optional<Comment> commentOpt = commentRepository.findById(commentId);
        if (commentOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "评论不存在");
            return result;
        }

        Comment comment = commentOpt.get();

        // 检查是否已点赞
        boolean exists = commentLikeRepository.existsByUserIdAndCommentId(userId, commentId);
        if (!exists) {
            result.put("success", false);
            result.put("message", "未点赞该评论");
            result.put("liked", false);
            result.put("commentLikeCount", comment.getLikeCount());
            return result;
        }

        // 删除点赞记录
        commentLikeRepository.deleteByUserIdAndCommentId(userId, commentId);

        // 更新评论的点赞数（原子操作）
        commentRepository.decrementLikeCount(commentId);

        // 重新获取最新的点赞数
        Comment updatedComment = commentRepository.findById(commentId).orElse(null);

        result.put("success", true);
        result.put("message", "取消点赞成功");
        result.put("liked", false);
        result.put("commentLikeCount", updatedComment != null ? updatedComment.getLikeCount() : 0);

        return result;
    }
}
