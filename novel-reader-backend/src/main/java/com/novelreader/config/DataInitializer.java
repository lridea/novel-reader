package com.novelreader.config;

import com.novelreader.entity.CrawlerConfig;
import com.novelreader.service.CrawlerConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 启动时初始化爬虫配置
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private CrawlerConfigService crawlerConfigService;

    @Override
    public void run(String... args) {
        log.info("========================================");
        log.info("🦞 开始初始化数据");
        log.info("========================================");

        try {
            initCrawlerConfigs();
        } catch (Exception e) {
            log.error("初始化数据失败: {}", e.getMessage(), e);
        }

        log.info("========================================");
        log.info("🦞 数据初始化完成");
        log.info("========================================");
    }

    /**
     * 初始化爬虫配置
     */
    private void initCrawlerConfigs() {
        log.info("检查爬虫配置...");

        // 检查刺猬猫配置
        CrawlerConfig ciweimaoConfig = crawlerConfigService.findByPlatform("ciweimao");
        if (ciweimaoConfig == null) {
            ciweimaoConfig = new CrawlerConfig();
            ciweimaoConfig.setPlatform("ciweimao");
            ciweimaoConfig.setBaseUrl("https://mip.ciweimao.com/");
            ciweimaoConfig.setEnabled(1);
            ciweimaoConfig.setTags("[\"玄幻\", \"修仙\", \"都市\", \"系统\"]");
            ciweimaoConfig.setCrawlInterval(7200);
            ciweimaoConfig.setMaxRetry(3);
            crawlerConfigService.save(ciweimaoConfig);
            log.info("创建刺猬猫爬虫配置");
        } else {
            log.info("刺猬猫爬虫配置已存在");
        }

        // 检查SF轻小说配置
        CrawlerConfig sfConfig = crawlerConfigService.findByPlatform("sf");
        if (sfConfig == null) {
            sfConfig = new CrawlerConfig();
            sfConfig.setPlatform("sf");
            sfConfig.setBaseUrl("https://book.sfacg.com/");
            sfConfig.setEnabled(1);
            sfConfig.setTags("[\"玄幻\", \"轻小说\", \"二次元\"]");
            sfConfig.setCrawlInterval(7200);
            sfConfig.setMaxRetry(3);
            crawlerConfigService.save(sfConfig);
            log.info("创建SF轻小说爬虫配置");
        } else {
            log.info("SF轻小说爬虫配置已存在");
        }

        // 检查次元姬配置
        CrawlerConfig ciyuanjiConfig = crawlerConfigService.findByPlatform("ciyuanji");
        if (ciyuanjiConfig == null) {
            ciyuanjiConfig = new CrawlerConfig();
            ciyuanjiConfig.setPlatform("ciyuanji");
            ciyuanjiConfig.setBaseUrl("https://www.ciyuanji.com/");
            ciyuanjiConfig.setEnabled(1);
            ciyuanjiConfig.setTags("[\"玄幻\", \"仙侠\", \"都市\"]");
            ciyuanjiConfig.setCrawlInterval(7200);
            ciyuanjiConfig.setMaxRetry(3);
            crawlerConfigService.save(ciyuanjiConfig);
            log.info("创建次元姬爬虫配置");
        } else {
            log.info("次元姬爬虫配置已存在");
        }

        log.info("爬虫配置初始化完成");
    }
}
