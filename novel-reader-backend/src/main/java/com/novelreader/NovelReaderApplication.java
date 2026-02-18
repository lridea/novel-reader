package com.novelreader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Novel Reader Application
 * 读书网站后端 - 爬虫核心功能
 */
@SpringBootApplication
@EnableScheduling
public class NovelReaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(NovelReaderApplication.class, args);
        System.out.println("========================================");
        System.out.println("Novel Reader Backend Started!");
        System.out.println("📚 支持平台: 刺猬猫、SF轻小说、次元姬");
        System.out.println("🤖 爬虫核心功能: 已启动");
        System.out.println("📖 AI概括服务: 已启动");
        System.out.println("========================================");
    }
}
