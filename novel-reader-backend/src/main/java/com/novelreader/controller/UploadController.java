package com.novelreader.controller;

import com.novelreader.entity.User;
import com.novelreader.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Autowired
    private UserRepository userRepository;

    @Value("${upload.avatar.path:#{null}}")
    private String avatarUploadPath;

    @Value("${upload.avatar.max-size:2097152}")
    private long maxFileSize;

    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};

    private Path getUploadDir() {
        if (avatarUploadPath != null && !avatarUploadPath.isEmpty()) {
            Path path = Paths.get(avatarUploadPath);
            if (path.isAbsolute()) {
                return path;
            }
        }
        String userHome = System.getProperty("user.home");
        return Paths.get(userHome, "novel-reader", "uploads", "avatar");
    }

    private void deleteOldAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isEmpty() || !avatarUrl.startsWith("/uploads/avatar/")) {
            return;
        }
        try {
            String filename = avatarUrl.substring("/uploads/avatar/".length());
            Path filePath = getUploadDir().resolve(filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("删除旧头像: {}", filePath);
            }
        } catch (IOException e) {
            log.warn("删除旧头像失败: {}", e.getMessage());
        }
    }

    @PostMapping("/avatar")
    public Map<String, Object> uploadAvatar(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = new HashMap<>();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
            "anonymousUser".equals(authentication.getPrincipal())) {
            result.put("success", false);
            result.put("message", "未登录");
            return result;
        }

        Long userId = (Long) authentication.getPrincipal();

        if (file.isEmpty()) {
            result.put("success", false);
            result.put("message", "请选择文件");
            return result;
        }

        if (file.getSize() > maxFileSize) {
            result.put("success", false);
            result.put("message", "文件大小不能超过2MB");
            return result;
        }

        String contentType = file.getContentType();
        boolean validType = false;
        for (String allowedType : ALLOWED_TYPES) {
            if (allowedType.equals(contentType)) {
                validType = true;
                break;
            }
        }
        if (!validType) {
            result.put("success", false);
            result.put("message", "只支持JPEG、PNG、GIF、WEBP格式的图片");
            return result;
        }

        try {
            Optional<User> userOpt = userRepository.findById(userId);
            String oldAvatarUrl = userOpt.map(User::getAvatarUrl).orElse(null);

            Path uploadDir = getUploadDir();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            } else {
                extension = ".jpg";
            }

            String newFilename = userId + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
            Path filePath = uploadDir.resolve(newFilename);

            file.transferTo(filePath.toFile());

            String avatarUrl = "/uploads/avatar/" + newFilename;

            deleteOldAvatar(oldAvatarUrl);

            result.put("success", true);
            result.put("message", "上传成功");
            result.put("avatarUrl", avatarUrl);

            log.info("用户 {} 上传头像成功: {}", userId, avatarUrl);

        } catch (IOException e) {
            log.error("上传头像失败", e);
            result.put("success", false);
            result.put("message", "上传失败，请稍后重试");
        }

        return result;
    }
}
