package com.novelreader.service;

import com.novelreader.BaseTest;
import com.novelreader.entity.User;
import com.novelreader.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest extends BaseTest {

    @Autowired
    private UserService userService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testGetUserById_Success() {
        authService.register("getuser", "获取用户", "password123");
        User user = userRepository.findByUsername("getuser").orElseThrow();

        Map<String, Object> result = userService.getUserById(user.getId());

        assertTrue((Boolean) result.get("success"));
        Map<String, Object> userInfo = (Map<String, Object>) result.get("user");
        assertEquals("getuser", userInfo.get("username"));
    }

    @Test
    void testGetUserById_NotFound() {
        Map<String, Object> result = userService.getUserById(99999L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));
    }

    @Test
    void testUpdateUserProfile_Success() {
        authService.register("updateuser", "更新用户", "password123");
        User user = userRepository.findByUsername("updateuser").orElseThrow();

        Map<String, Object> result = userService.updateUserProfile(
            user.getId(), "新昵称", "https://example.com/avatar.jpg");

        assertTrue((Boolean) result.get("success"));
        assertEquals("更新成功", result.get("message"));

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("新昵称", updatedUser.getNickname());
        assertEquals("https://example.com/avatar.jpg", updatedUser.getAvatarUrl());
    }

    @Test
    void testUpdateUserProfile_NotFound() {
        Map<String, Object> result = userService.updateUserProfile(99999L, "昵称", null);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        authService.register("pwduser2", "密码用户2", "oldpassword");
        User user = userRepository.findByUsername("pwduser2").orElseThrow();

        Map<String, Object> result = userService.changePassword(
            user.getId(), "wrongpassword", "newpassword123");

        assertFalse((Boolean) result.get("success"));
        assertEquals("旧密码错误", result.get("message"));
    }

    @Test
    void testChangePassword_UserNotFound() {
        Map<String, Object> result = userService.changePassword(
            99999L, "oldpassword", "newpassword123");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));
    }

    @Test
    void testGetUserStats_Success() {
        Map<String, Object> registerResult = authService.register("statsuser", "统计用户", "password123");
        Map<String, Object> userInfo = (Map<String, Object>) registerResult.get("user");
        Long userId = ((Number) userInfo.get("id")).longValue();

        Map<String, Object> result = userService.getUserStats(userId);

        assertTrue((Boolean) result.get("success"));
        assertNotNull(result.get("favoriteCount"));
        assertNotNull(result.get("commentCount"));
    }

    @Test
    void testGetUserStats_NotFound() {
        Map<String, Object> result = userService.getUserStats(99999L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));
    }
}
