package com.novelreader.filter.trie;

/**
 * Trie树（字典树）
 */
public class TrieTree {

    /**
     * 根节点
     */
    private TrieNode root;

    public TrieTree() {
        this.root = new TrieNode();
    }

    /**
     * 插入敏感词
     */
    public void insert(String word) {
        if (word == null || word.isEmpty()) {
            return;
        }

        TrieNode node = root;
        for (char c : word.toCharArray()) {
            node.addChild(c);
            node = node.getChild(c);
        }
        node.setEnd(true);
        node.setWord(word);
    }

    /**
     * 批量插入敏感词
     */
    public void insertBatch(java.util.List<String> words) {
        if (words == null || words.isEmpty()) {
            return;
        }

        for (String word : words) {
            insert(word);
        }
    }

    /**
     * 搜索是否包含敏感词
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        for (int i = 0; i < text.length(); i++) {
            TrieNode node = root;
            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                if (!node.containsChild(c)) {
                    break;
                }
                node = node.getChild(c);
                if (node.isEnd()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 过滤文本（替换敏感词为***）
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            TrieNode node = root;
            int matchEnd = -1;

            for (int j = i; j < length; j++) {
                char c = text.charAt(j);
                if (!node.containsChild(c)) {
                    break;
                }
                node = node.getChild(c);
                if (node.isEnd()) {
                    matchEnd = j;
                }
            }

            if (matchEnd != -1) {
                // 匹配到敏感词，替换为***
                result.append("***");
                i = matchEnd;
            } else {
                // 未匹配到敏感词，保留原字符
                result.append(text.charAt(i));
            }
        }

        return result.toString();
    }

    /**
     * 获取文本中的所有敏感词
     */
    public java.util.List<String> getSensitiveWords(String text) {
        java.util.List<String> sensitiveWords = new java.util.ArrayList<>();

        if (text == null || text.isEmpty()) {
            return sensitiveWords;
        }

        java.util.Set<String> foundWords = new java.util.HashSet<>();

        for (int i = 0; i < text.length(); i++) {
            TrieNode node = root;
            for (int j = i; j < text.length(); j++) {
                char c = text.charAt(j);
                if (!node.containsChild(c)) {
                    break;
                }
                node = node.getChild(c);
                if (node.isEnd()) {
                    foundWords.add(node.getWord());
                }
            }
        }

        sensitiveWords.addAll(foundWords);
        return sensitiveWords;
    }
}
