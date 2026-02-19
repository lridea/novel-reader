package com.novelreader.service;

import com.novelreader.entity.FavoriteCategory;
import com.novelreader.entity.User;
import com.novelreader.repository.FavoriteCategoryRepository;
import com.novelreader.repository.UserRepository;
import com.novelreader.config.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FavoriteCategoryRepository favoriteCategoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public Map<String, Object> register(String username, String nickname, String password) {
        log.info("用户注册: username={}, nickname={}", username, nickname);

        Map<String, Object> result = new HashMap<>();

        if (username == null || !username.matches("^[a-zA-Z][a-zA-Z0-9_-]{3,15}$")) {
            result.put("success", false);
            result.put("message", "用户名必须以字母开头，只能包含字母、数字、下划线和横线，长度4-16个字符");
            return result;
        }

        if (userRepository.existsByUsername(username)) {
            result.put("success", false);
            result.put("message", "用户名已被使用");
            return result;
        }

        if (userRepository.existsByNickname(nickname)) {
            result.put("success", false);
            result.put("message", "昵称已被使用");
            return result;
        }

        User user = new User();
        user.setUsername(username);
        user.setNickname(nickname);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setEnabled(1);

        User savedUser = userRepository.save(user);

        FavoriteCategory defaultCategory = new FavoriteCategory();
        defaultCategory.setUserId(savedUser.getId());
        defaultCategory.setName("默认收藏夹");
        defaultCategory.setIsDefault(true);
        defaultCategory.setSortOrder(0);
        favoriteCategoryRepository.save(defaultCategory);

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
    public Map<String, Object> login(String username, String password) {
        log.info("用户登录: username={}", username);

        Map<String, Object> result = new HashMap<>();

        // 查找用户
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
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
            result.put("message", "用户名或密码错误");
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
