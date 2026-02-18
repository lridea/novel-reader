package com.novelreader.service;

import com.novelreader.entity.Comment;
import com.novelreader.entity.Novel;
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
    private NovelRepository novelRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 获取评论列表（分页）
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

        // 分页查询评论
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Comment> commentPage;

        if (floor != null && floor == 2 && parentId != null) {
            // 查询回复
            commentPage = commentRepository.findByParentIdAndDeletedOrderByCreatedAtAsc(parentId, 0, pageable);
        } else {
            // 查询顶层评论
            commentPage = commentRepository.findByNovelIdAndFloorAndDeletedOrderByCreatedAtDesc(novelId, 1, 0, pageable);
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

        // 获取用户信息
        Optional<com.novelreader.entity.User> userOpt = userRepository.findById(comment.getUserId());
        if (userOpt.isPresent()) {
            com.novelreader.entity.User user = userOpt.get();
            Comment.UserInfo userInfo = new Comment.UserInfo();
            userInfo.setId(user.getId());
            userInfo.setUsername(user.getUsername());
            userInfo.setNickname(user.getNickname());
            userInfo.setAvatar(user.getAvatar());
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
                    parentUserInfo.setAvatar(parentUser.getAvatar());
                    commentInfo.put("parentUser", parentUserInfo);
                }
            }
        }

        // 当前用户是否已点赞（暂未实现点赞功能）
        commentInfo.put("liked", false);

        // 当前用户是否为评论作者
        if (userId != null) {
            commentInfo.put("isOwner", comment.getUserId().equals(userId));
        } else {
            commentInfo.put("isOwner", false);
        }

        return commentInfo;
    }
}
