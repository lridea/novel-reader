package com.novelreader.controller;

import com.novelreader.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Map<String, Object> getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/profile")
    public Map<String, Object> updateProfile(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        String nickname = request.get("nickname");
        String avatarUrl = request.get("avatarUrl");

        return userService.updateUserProfile(userId, nickname, avatarUrl);
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");

        return userService.changePassword(userId, oldPassword, newPassword);
    }

    /**
     * 获取用户统计数据
     */
    @GetMapping("/stats")
    public Map<String, Object> getUserStats() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            Map<String, Object> error = new java.util.HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserStats(userId);
    }
}
