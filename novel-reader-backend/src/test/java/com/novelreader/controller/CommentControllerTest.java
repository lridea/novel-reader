package com.novelreader.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.novelreader.entity.User;
import com.novelreader.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CommentController测试
 */
@WebMvcTest(CommentController.class)
@ActiveProfiles("test")
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommentService commentService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // 创建测试用户
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setNickname("测试用户");
        testUser.setAvatar("https://example.com/avatar.jpg");
    }

    @Test
    @WithMockUser
    void testGetComments() throws Exception {
        // Given
        Long novelId = 1L;
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("content", new Object[]{});
        mockResponse.put("totalElements", 0);
        mockResponse.put("totalPages", 0);
        mockResponse.put("size", 10);
        mockResponse.put("number", 0);

        when(commentService.getComments(eq(novelId), anyInt(), anyInt(), isNull(), isNull(), anyLong()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/comments/novel/{novelId}", novelId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void testGetComments_Unauthenticated() throws Exception {
        // Given
        Long novelId = 1L;
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("content", new Object[]{});
        mockResponse.put("totalElements", 0);
        mockResponse.put("totalPages", 0);
        mockResponse.put("size", 10);
        mockResponse.put("number", 0);

        when(commentService.getComments(eq(novelId), anyInt(), anyInt(), isNull(), isNull(), isNull()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/api/comments/novel/{novelId}", novelId)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser", id = "1")
    void testAddComment() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("novelId", 1L);
        request.put("parentId", null);
        request.put("floor", 1);
        request.put("content", "这本书太好看了！");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("message", "评论成功");
        mockResponse.put("novelCommentCount", 11);

        when(commentService.addComment(eq(1L), eq(1L), isNull(), eq(1), anyString()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("评论成功"))
                .andExpect(jsonPath("$.novelCommentCount").value(11));
    }

    @Test
    void testAddComment_Unauthenticated() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("novelId", 1L);
        request.put("parentId", null);
        request.put("floor", 1);
        request.put("content", "这本书太好看了！");

        // When & Then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("请先登录"));
    }

    @Test
    @WithMockUser(username = "testuser", id = "1")
    void testAddReplyComment() throws Exception {
        // Given
        Map<String, Object> request = new HashMap<>();
        request.put("novelId", 1L);
        request.put("parentId", 1L);
        request.put("floor", 2);
        request.put("content", "同意！");

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("success", true);
        mockResponse.put("message", "评论成功");
        mockResponse.put("novelCommentCount", 11);

        when(commentService.addComment(eq(1L), eq(1L), eq(1L), eq(2), anyString()))
                .thenReturn(mockResponse);

        // When & Then
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("评论成功"))
                .andExpect(jsonPath("$.novelCommentCount").value(11));
    }
}
