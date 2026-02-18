package com.novelreader.filter;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 过滤结果
 */
@Data
public class FilterResult {

    /**
     * 是否包含敏感词
     */
    private boolean sensitive;

    /**
     * 过滤后的文本
     */
    private String filteredText;

    /**
     * 敏感词列表
     */
    private List<String> sensitiveWords;

    public FilterResult() {
        this.sensitive = false;
        this.filteredText = "";
        this.sensitiveWords = new ArrayList<>();
    }

    public FilterResult(boolean sensitive, String filteredText, List<String> sensitiveWords) {
        this.sensitive = sensitive;
        this.filteredText = filteredText;
        this.sensitiveWords = sensitiveWords;
    }

    /**
     * 创建成功结果（无敏感词）
     */
    public static FilterResult success(String text) {
        return new FilterResult(false, text, new ArrayList<>());
    }

    /**
     * 创建失败结果（包含敏感词）
     */
    public static FilterResult fail(String filteredText, List<String> sensitiveWords) {
        return new FilterResult(true, filteredText, sensitiveWords);
    }
}
