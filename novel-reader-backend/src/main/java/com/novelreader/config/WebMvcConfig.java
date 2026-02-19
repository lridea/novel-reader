package com.novelreader.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${upload.avatar.path:#{null}}")
    private String avatarUploadPath;

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

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadDir = getUploadDir();
        String absolutePath = uploadDir.toAbsolutePath().toString();
        registry.addResourceHandler("/uploads/avatar/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
