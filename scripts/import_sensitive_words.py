#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
敏感词导入脚本
用于从文本文件导入敏感词到数据库
"""

import requests
import json
import sys
from typing import List

# 配置
API_BASE_URL = "http://localhost:8080/api/sensitive-words"
TOKEN = "YOUR_TOKEN"  # 替换为你的Token


def import_words(words: List[str]) -> dict:
    """
    批量导入敏感词

    Args:
        words: 敏感词列表

    Returns:
        API响应
    """
    url = f"{API_BASE_URL}/import"
    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {TOKEN}"
    }
    data = {
        "words": words
    }

    try:
        response = requests.post(url, headers=headers, json=data)
        response.raise_for_status()
        return response.json()
    except requests.exceptions.RequestException as e:
        print(f"请求失败: {e}")
        return {"success": False, "message": str(e)}


def parse_sensitive_words_file(file_path: str) -> dict:
    """
    解析敏感词文件

    格式：
    ## 分类名称
    敏感词1
    敏感词2
    ...

    Args:
        file_path: 文件路径

    Returns:
        敏感词字典: {分类: [敏感词列表]}
    """
    sensitive_words = {}
    current_category = None

    with open(file_path, 'r', encoding='utf-8') as f:
        for line in f:
            line = line.strip()

            # 跳过空行和注释
            if not line or line.startswith('#'):
                continue

            # 判断是否为分类标题（以##开头）
            if line.startswith('##'):
                current_category = line[2:].strip()
                sensitive_words[current_category] = []
            else:
                # 添加敏感词
                if current_category:
                    sensitive_words[current_category].append(line)

    return sensitive_words


def main():
    """主函数"""
    if len(sys.argv) < 2:
        print("使用方法: python3 import_sensitive_words.py <敏感词文件>")
        sys.exit(1)

    file_path = sys.argv[1]

    # 解析敏感词文件
    print(f"正在读取敏感词文件: {file_path}")
    sensitive_words = parse_sensitive_words_file(file_path)

    # 统计信息
    total_words = sum(len(words) for words in sensitive_words.values())
    print(f"共读取 {len(sensitive_words)} 个分类，{total_words} 个敏感词")

    # 按分类导入
    for category, words in sensitive_words.items():
        print(f"\n正在导入分类: {category} ({len(words)} 个敏感词)")

        # 分批导入（每批100个）
        batch_size = 100
        for i in range(0, len(words), batch_size):
            batch = words[i:i + batch_size]
            print(f"  批次 {i//batch_size + 1}: 导入 {len(batch)} 个敏感词...", end=" ")

            result = import_words(batch)

            if result.get("success"):
                success_count = result.get("successCount", 0)
                skip_count = result.get("skipCount", 0)
                print(f"成功 {success_count}, 跳过 {skip_count}")
            else:
                print(f"失败: {result.get('message', '未知错误')}")

    print("\n导入完成！")


if __name__ == "__main__":
    main()
