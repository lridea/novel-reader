package com.novelreader.service;

import com.novelreader.entity.Comment;
import com.novelreader.entity.Novel;
import com.novelreader.entity.User;
import com.novelreader.repository.CommentRepository;
import com.novelreader.repository.NovelRepository;
import com.novelreader.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * CommentService测试
 */
@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private NovelRepository novelRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CommentService commentService;

    private User testUser;
    private Novel testNovel;
    private Comment testComment;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        testUser.setAvatar("https://example.com/avatar.jpg");

        // 创建测试小说
        testNovel = new Novel();
        testNovel.setId(1L);
        testNovel.setPlatform("ciweimao");
        testNovel.setNovelId("1001");
        testNovel.setTitle("测试小说");
        testNovel.setFavoriteCount(100);
        testNovel.setCommentCount(10);

        // 创建测试评论
        testComment = new Comment();
        testComment.setId(1L);
        testComment.setUserId(1L);
        testComment.setNovelId(1L);
        testComment.setParentId(null);
        testComment.setFloor(1);
        testComment.setContent("测试评论");
        testComment.setLikeCount(0);
        testComment.setReplyCount(0);
    }

    @Test
    void testAddTopLevelComment() {
        // Given
        Long userId = 1L;
        Long novelId = 1L;
        Long parentId = null;
        Integer floor = 1;
        String content = "这本书太好看了！";

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(testNovel));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // When
        Map<String, Object> result = commentService.addComment(userId, novelId, parentId, floor, content);

        // Then
        assertTrue((Boolean) result.get("success"));
        assertEquals("评论成功", result.get("message"));

        // 验证调用
        verify(novelRepository).incrementCommentCount(novelId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void testAddReplyComment() {
        // Given
        Long userId = 2L;
        Long novelId = 1L;
        Long parentId = 1L;
        Integer floor = 2;
        String content = "同意！";

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setNickname("用户2");
        user2.setAvatar("https://example.com/avatar2.jpg");

        Comment parentComment = new Comment();
        parentComment.setId(1L);
        parentComment.setUserId(1L);
        parentComment.setNovelId(1L);
        parentComment.setParentId(null);
        parentComment.setFloor(1);
        parentComment.setContent("测试评论");
        parentComment.setLikeCount(0);
        parentComment.setReplyCount(0);

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(testNovel));
        when(commentRepository.findById(parentId)).thenReturn(Optional.of(parentComment));
        when(commentRepository.save(any(Comment.class))).thenReturn(testComment);
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user2), Optional.of(testUser));

        // When
        Map<String, Object> result = commentService.addComment(userId, novelId, parentId, floor, content);

        // Then
        assertTrue((Boolean) result.get("success"));
        assertEquals("评论成功", result.get("message"));

        // 验证调用
        verify(novelRepository).incrementCommentCount(novelId);
        verify(commentRepository).incrementReplyCount(parentId);
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    void testAddComment_NovelNotFound() {
        // Given
        Long userId = 1L;
        Long novelId = 999L;
        Long parentId = null;
        Integer floor = 1;
        String content = "测试评论";

        when(novelRepository.findById(novelId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = commentService.addComment(userId, novelId, parentId, floor, content);

        // Then
        assertFalse((Boolean) result.get("success"));
        assertEquals("小说不存在", result.get("message"));

        // 验证不调用
        verify(novelRepository, never()).incrementCommentCount(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testAddComment_ParentNotFound() {
        // Given
        Long userId = 1L;
        Long novelId = 1L;
        Long parentId = 999L;
        Integer floor = 2;
        String content = "测试回复";

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(testNovel));
        when(commentRepository.findById(parentId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = commentService.addComment(userId, novelId, parentId, floor, content);

        // Then
        assertFalse((Boolean) result.get("success"));
        assertEquals("父评论不存在", result.get("message"));

        // 验证不调用
        verify(novelRepository, never()).incrementCommentCount(anyLong());
        verify(commentRepository, never()).save(any(Comment.class));
    }

    @Test
    void testGetComments() {
        // Given
        Long novelId = 1L;
        Integer page = 0;
        Integer size = 10;
        Integer floor = 1;
        Long parentId = null;
        Long userId = 1L;

        when(novelRepository.findById(novelId)).thenReturn(Optional.of(testNovel));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));

        // When
        Map<String, Object> result = commentService.getComments(novelId, page, size, floor, parentId, userId);

        // Then
        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("content"));
    }

    @Test
    void testGetComments_NovelNotFound() {
        // Given
        Long novelId = 999L;
        Integer page = 0;
        Integer size = 10;
        Integer floor = 1;
        Long parentId = null;
        Long userId = 1L;

        when(novelRepository.findById(novelId)).thenReturn(Optional.empty());

        // When
        Map<String, Object> result = commentService.getComments(novelId, page, size, floor, parentId, userId);

        // Then
        assertFalse((Boolean) result.get("success"));
        assertEquals("小说不存在", result.get("message"));
    }
}
