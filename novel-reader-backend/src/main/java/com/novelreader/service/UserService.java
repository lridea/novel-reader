package com.novelreader.service;

import com.novelreader.entity.User;
import com.novelreader.repository.CommentRepository;
import com.novelreader.repository.FavoriteRepository;
import com.novelreader.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取用户信息
     */
    public Map<String, Object> getUserById(Long userId) {
        log.info("获取用户信息: userId={}", userId);

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
     * 更新用户信息
     */
    public Map<String, Object> updateUserProfile(Long userId, String nickname, String avatarUrl) {
        log.info("更新用户信息: userId={}, nickname={}", userId, nickname);

        Map<String, Object> result = new HashMap<>();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        User user = userOpt.get();

        // 更新字段
        if (nickname != null && !nickname.isEmpty()) {
            user.setNickname(nickname);
        }
        if (avatarUrl != null && !avatarUrl.isEmpty()) {
            user.setAvatarUrl(avatarUrl);
        }

        User savedUser = userRepository.save(user);

        result.put("success", true);
        result.put("message", "更新成功");
        result.put("user", toUserInfo(savedUser));

        return result;
    }

    /**
     * 修改密码
     */
    public Map<String, Object> changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("修改密码: userId={}", userId);

        Map<String, Object> result = new HashMap<>();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        User user = userOpt.get();

        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            result.put("success", false);
            result.put("message", "旧密码错误");
            return result;
        }

        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        result.put("success", true);
        result.put("message", "密码修改成功");

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
        userInfo.put("createdAt", user.getCreatedAt());
        userInfo.put("lastLoginTime", user.getLastLoginTime());
        return userInfo;
    }

    /**
     * 获取用户统计数据
     */
    public Map<String, Object> getUserStats(Long userId) {
        log.info("获取用户统计数据: userId={}", userId);

        Map<String, Object> result = new HashMap<>();

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户不存在");
            return result;
        }

        long favoriteCount = favoriteRepository.countByUserId(userId);
        long commentCount = commentRepository.countByUserIdAndDeleted(userId, 0);

        result.put("success", true);
        result.put("favoriteCount", favoriteCount);
        result.put("commentCount", commentCount);

        return result;
    }
}
