package com.novelreader.service;

import com.novelreader.BaseTest;
import com.novelreader.entity.User;
import com.novelreader.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest extends BaseTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testRegister_Success() {
        Map<String, Object> result = authService.register("testuser", "测试用户", "password123");

        assertTrue((Boolean) result.get("success"));
        assertEquals("注册成功", result.get("message"));
        assertNotNull(result.get("token"));
        assertNotNull(result.get("user"));

        Map<String, Object> userInfo = (Map<String, Object>) result.get("user");
        assertEquals("testuser", userInfo.get("username"));
        assertEquals("测试用户", userInfo.get("nickname"));
        assertEquals("USER", userInfo.get("role"));
    }

    @Test
    void testRegister_UsernameExists() {
        authService.register("existinguser", "用户1", "password123");
        Map<String, Object> result = authService.register("existinguser", "用户2", "password123");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名已被使用", result.get("message"));
    }

    @Test
    void testRegister_NicknameExists() {
        authService.register("user1", "samename", "password123");
        Map<String, Object> result = authService.register("user2", "samename", "password123");

        assertFalse((Boolean) result.get("success"));
        assertEquals("昵称已被使用", result.get("message"));
    }

    @Test
    void testRegister_InvalidUsername() {
        Map<String, Object> result = authService.register("123user", "用户", "password123");

        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("用户名必须以字母开头"));
    }

    @Test
    void testRegister_ShortPassword() {
        Map<String, Object> result = authService.register("validuser", "用户", "123");

        assertFalse((Boolean) result.get("success"));
        assertTrue(((String) result.get("message")).contains("密码长度"));
    }

    @Test
    void testLogin_Success() {
        authService.register("logintest", "登录测试", "password123");
        Map<String, Object> result = authService.login("logintest", "password123");

        assertTrue((Boolean) result.get("success"));
        assertEquals("登录成功", result.get("message"));
        assertNotNull(result.get("token"));
    }

    @Test
    void testLogin_WrongPassword() {
        authService.register("passwrong", "密码错误", "password123");
        Map<String, Object> result = authService.login("passwrong", "wrongpassword");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
    }

    @Test
    void testLogin_UserNotFound() {
        Map<String, Object> result = authService.login("nonexistent", "password123");

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户名或密码错误", result.get("message"));
    }

    @Test
    void testLogin_UserDisabled() {
        authService.register("disableduser", "禁用用户", "password123");
        User user = userRepository.findByUsername("disableduser").orElseThrow();
        user.setEnabled(0);
        userRepository.save(user);

        Map<String, Object> result = authService.login("disableduser", "password123");

        assertFalse((Boolean) result.get("success"));
        assertEquals("账号已被禁用", result.get("message"));
    }

    @Test
    void testGetCurrentUser_Success() {
        authService.register("currentuser", "当前用户", "password123");
        User user = userRepository.findByUsername("currentuser").orElseThrow();

        Map<String, Object> result = authService.getCurrentUser(user.getId());

        assertTrue((Boolean) result.get("success"));
        Map<String, Object> userInfo = (Map<String, Object>) result.get("user");
        assertEquals("currentuser", userInfo.get("username"));
    }

    @Test
    void testGetCurrentUser_NotFound() {
        Map<String, Object> result = authService.getCurrentUser(99999L);

        assertFalse((Boolean) result.get("success"));
        assertEquals("用户不存在", result.get("message"));
    }
}
