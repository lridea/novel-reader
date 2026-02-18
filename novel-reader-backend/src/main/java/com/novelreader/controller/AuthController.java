package com.novelreader.controller;

import com.novelreader.service.AuthService;
import com.novelreader.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        if (username == null || username.isEmpty() ||
            email == null || email.isEmpty() ||
            password == null || password.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "参数不完整");
            return error;
        }

        return authService.register(username, email, password);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String email = request.get("email");
        String password = request.get("password");

        String loginAccount = (username != null && !username.isEmpty()) ? username : email;

        if (loginAccount == null || loginAccount.isEmpty() ||
            password == null || password.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "参数不完整");
            return error;
        }

        return authService.login(loginAccount, password);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "未登录");
            return error;
        }

        Long userId = (Long) authentication.getPrincipal();
        return userService.getUserById(userId);
    }

    /**
     * 用户注销
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "注销成功");
        return result;
    }
}
