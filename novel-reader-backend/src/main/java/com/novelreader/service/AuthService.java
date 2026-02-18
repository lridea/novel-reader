package com.novelreader.service;

import com.novelreader.entity.User;
import com.novelreader.repository.UserRepository;
import com.novelreader.config.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 认证服务
 */
@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户注册
     */
    public Map<String, Object> register(String username, String email, String password) {
        log.info("用户注册: username={}, email={}", username, email);

        Map<String, Object> result = new HashMap<>();

        // 检查用户名是否已存在
        if (userRepository.existsByUsername(username)) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(email)) {
            result.put("success", false);
            result.put("message", "邮箱已被注册");
            return result;
        }

        // 创建用户
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEnabled(1);

        User savedUser = userRepository.save(user);

        // 生成Token
        String token = jwtUtil.generateToken(savedUser.getId(), savedUser.getUsername(), savedUser.getRole());

        result.put("success", true);
        result.put("message", "注册成功");
        result.put("token", token);
        result.put("user", toUserInfo(savedUser));

        return result;
    }

    /**
     * 用户登录
     */
    public Map<String, Object> login(String email, String password) {
        log.info("用户登录: email={}", email);

        Map<String, Object> result = new HashMap<>();

        // 查找用户
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "邮箱或密码错误");
            return result;
        }

        User user = userOpt.get();

        // 检查账号是否禁用
        if (user.getEnabled() == 0) {
            result.put("success", false);
            result.put("message", "账号已被禁用");
            return result;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            result.put("success", false);
            result.put("message", "邮箱或密码错误");
            return result;
        }

        // 更新最后登录时间
        user.setLastLoginTime(java.time.LocalDateTime.now());
        userRepository.save(user);

        // 生成Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("token", token);
        result.put("user", toUserInfo(user));

        return result;
    }

    /**
     * 获取当前用户信息
     */
    public Map<String, Object> getCurrentUser(Long userId) {
        log.info("获取当前用户信息: userId={}", userId);

        Map<String, Object> result = new HashMap<>();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        User user = userOpt.get();

        result.put("success", true);
        result.put("user", toUserInfo(user));

        return result;
    }

    /**
     * 转换为用户信息
     */
    private Map<String, Object> toUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("username", user.getUsername());
        userInfo.put("email", user.getEmail());
        userInfo.put("nickname", user.getNickname());
        userInfo.put("avatarUrl", user.getAvatarUrl());
        userInfo.put("role", user.getRole());
        return userInfo;
    }
}
