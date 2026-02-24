package com.novelreader.service;

import com.novelreader.BaseTest;
import com.novelreader.entity.Comment;
import com.novelreader.entity.Novel;
import com.novelreader.repository.CommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CommentServiceTest extends BaseTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private NovelService novelService;

    @Autowired
    private CommentRepository commentRepository;

    private Novel createTestNovel(String title) {
        Novel novel = new Novel();
        novel.setTitle(title);
        novel.setAuthor("测试作者");
        novel.setPlatform("test");
        novel.setNovelId("novel_" + System.currentTimeMillis());
        novel.setStatus(0);
        novel.setWordCount(100000L);
        novel.setFavoriteCount(0);
        novel.setCommentCount(0);
        novel.setDeleted(0);
        return novelService.save(novel);
    }

    @Test
    void testGetComments() {
        Novel novel = createTestNovel("评论测试小说");
        commentService.addComment(1L, novel.getId(), null, null, 1, "测试评论");

        Map<String, Object> result = commentService.getComments(novel.getId(), 0, 10, null, null, 1L);

        assertTrue((Boolean) result.get("success"));
    }

    @Test
    void testGetComments_NovelNotFound() {
        Map<String, Object> result = commentService.getComments(99999L, 0, 10, null, null, 1L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("小说不存在", result.get("message"));
    }

    @Test
    void testAddComment_Success() {
        Novel novel = createTestNovel("添加评论小说");

        Map<String, Object> result = commentService.addComment(1L, novel.getId(), null, null, 1, "这是一条测试评论");

        assertTrue((Boolean) result.get("success"));
        assertEquals("评论成功", result.get("message"));
    }

    @Test
    void testAddComment_EmptyContent() {
        Novel novel = createTestNovel("空内容评论");

        Map<String, Object> result = commentService.addComment(1L, novel.getId(), null, null, 1, "");

        assertFalse((Boolean) result.get("success"));
        assertEquals("评论内容不能为空", result.get("message"));
    }

    @Test
    void testAddComment_ContentTooLong() {
        Novel novel = createTestNovel("内容过长评论");
        String longContent = "a".repeat(2001);

        Map<String, Object> result = commentService.addComment(1L, novel.getId(), null, null, 1, longContent);

        assertFalse((Boolean) result.get("success"));
        assertEquals("评论内容不能超过2000个字符", result.get("message"));
    }

    @Test
    void testAddComment_NovelNotFound() {
        Map<String, Object> result = commentService.addComment(1L, 99999L, null, null, 1, "评论内容");

        assertFalse((Boolean) result.get("success"));
        assertEquals("小说不存在", result.get("message"));
    }

    @Test
    void testLikeComment_Success() {
        Novel novel = createTestNovel("点赞测试");
        Map<String, Object> commentResult = commentService.addComment(1L, novel.getId(), null, null, 1, "点赞评论");
        Map<String, Object> commentMap = (Map<String, Object>) commentResult.get("comment");
        Long commentId = ((Number) commentMap.get("id")).longValue();

        Map<String, Object> result = commentService.likeComment(2L, commentId);

        assertTrue((Boolean) result.get("success"));
        assertTrue((Boolean) result.get("liked"));
    }

    @Test
    void testLikeComment_AlreadyLiked() {
        Novel novel = createTestNovel("重复点赞");
        Map<String, Object> commentResult = commentService.addComment(1L, novel.getId(), null, null, 1, "重复点赞评论");
        Map<String, Object> commentMap = (Map<String, Object>) commentResult.get("comment");
        Long commentId = ((Number) commentMap.get("id")).longValue();

        commentService.likeComment(2L, commentId);
        Map<String, Object> result = commentService.likeComment(2L, commentId);

        assertFalse((Boolean) result.get("success"));
        assertEquals("已点赞该评论", result.get("message"));
    }

    @Test
    void testLikeComment_CommentNotFound() {
        Map<String, Object> result = commentService.likeComment(1L, 99999L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("评论不存在", result.get("message"));
    }

    @Test
    void testUnlikeComment_Success() {
        Novel novel = createTestNovel("取消点赞");
        Map<String, Object> commentResult = commentService.addComment(1L, novel.getId(), null, null, 1, "取消点赞评论");
        Map<String, Object> commentMap = (Map<String, Object>) commentResult.get("comment");
        Long commentId = ((Number) commentMap.get("id")).longValue();

        commentService.likeComment(2L, commentId);
        Map<String, Object> result = commentService.unlikeComment(2L, commentId);

        assertTrue((Boolean) result.get("success"));
        assertFalse((Boolean) result.get("liked"));
    }

    @Test
    void testUnlikeComment_NotLiked() {
        Novel novel = createTestNovel("未点赞");
        Map<String, Object> commentResult = commentService.addComment(1L, novel.getId(), null, null, 1, "未点赞评论");
        Map<String, Object> commentMap = (Map<String, Object>) commentResult.get("comment");
        Long commentId = ((Number) commentMap.get("id")).longValue();

        Map<String, Object> result = commentService.unlikeComment(2L, commentId);

        assertFalse((Boolean) result.get("success"));
        assertEquals("未点赞该评论", result.get("message"));
    }

    @Test
    void testUnlikeComment_CommentNotFound() {
        Map<String, Object> result = commentService.unlikeComment(1L, 99999L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("评论不存在", result.get("message"));
    }
}
