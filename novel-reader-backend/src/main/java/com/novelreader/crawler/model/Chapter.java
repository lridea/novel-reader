package com.novelreader.crawler.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 章节模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Chapter {

    /**
     * 章节序号
     */
    private Integer chapter;

    /**
     * 章节ID
     */
    private String chapterId;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节内容
     */
    private String content;

    public Chapter(Integer chapter, String title, String content) {
        this.chapter = chapter;
        this.title = title;
        this.content = content;
    }
}
