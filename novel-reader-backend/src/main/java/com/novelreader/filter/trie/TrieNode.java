package com.novelreader.filter.trie;

/**
 * Trie树节点
 */
public class TrieNode {

    /**
     * 子节点
     */
    private java.util.Map<Character, TrieNode> children;

    /**
     * 是否为敏感词结尾
     */
    private boolean isEnd;

    /**
     * 敏感词内容（用于返回匹配到的敏感词）
     */
    private String word;

    public TrieNode() {
        this.children = new java.util.HashMap<>();
        this.isEnd = false;
        this.word = null;
    }

    public java.util.Map<Character, TrieNode> getChildren() {
        return children;
    }

    public void setChildren(java.util.Map<Character, TrieNode> children) {
        this.children = children;
    }

    public boolean isEnd() {
        return isEnd;
    }

    public void setEnd(boolean end) {
        isEnd = end;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    /**
     * 获取子节点
     */
    public TrieNode getChild(char c) {
        return children.get(c);
    }

    /**
     * 添加子节点
     */
    public void addChild(char c) {
        if (!children.containsKey(c)) {
            children.put(c, new TrieNode());
        }
    }

    /**
     * 是否包含子节点
     */
    public boolean containsChild(char c) {
        return children.containsKey(c);
    }
}
