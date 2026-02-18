#!/bin/bash

# Novel Reader - 测试运行脚本
# 用于运行单元测试并生成覆盖率报告

set -e

echo "=================================="
echo "  Novel Reader 测试运行脚本"
echo "=================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 项目根目录
PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/novel-reader-backend"
cd "$PROJECT_DIR"

echo "当前目录: $(pwd)"
echo ""

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}错误: Maven 未安装${NC}"
    echo ""
    echo "请安装 Maven："
    echo "  - Ubuntu/Debian: sudo apt-get install maven"
    echo "  - macOS: brew install maven"
    echo "  - Windows: 下载并安装 Maven (https://maven.apache.org/download.cgi)"
    exit 1
fi

echo -e "${GREEN}✓ Maven 已安装${NC}"
echo "Maven 版本: $(mvn -version | head -n 1)"
echo ""

# 显示菜单
echo "请选择操作："
echo "  1. 运行所有测试"
echo "  2. 运行 Service 层测试"
echo "  3. 运行 Config 层测试"
echo "  4. 运行特定测试类"
echo "  5. 运行特定测试方法"
echo "  6. 生成测试覆盖率报告"
echo "  7. 清理并重新运行所有测试"
echo "  q. 退出"
echo ""
read -p "请输入选项 (1-7, q): " choice

echo ""

# 根据选择执行操作
case $choice in
    1)
        echo "=================================="
        echo "  运行所有测试"
        echo "=================================="
        echo ""
        mvn clean test
        echo ""
        echo -e "${GREEN}✓ 所有测试完成${NC}"
        ;;
    2)
        echo "=================================="
        echo "  运行 Service 层测试"
        echo "=================================="
        echo ""
        mvn test -Dtest=AuthServiceTest,UserServiceTest,FavoriteServiceTest,FavoriteCategoryServiceTest
        echo ""
        echo -e "${GREEN}✓ Service 层测试完成${NC}"
        ;;
    3)
        echo "=================================="
        echo "  运行 Config 层测试"
        echo "=================================="
        echo ""
        mvn test -Dtest=JwtUtilTest
        echo ""
        echo -e "${GREEN}✓ Config 层测试完成${NC}"
        ;;
    4)
        read -p "请输入测试类名 (例如: AuthServiceTest): " testClass
        echo ""
        echo "=================================="
        echo "  运行测试: $testClass"
        echo "=================================="
        echo ""
        mvn test -Dtest="$testClass"
        echo ""
        echo -e "${GREEN}✓ 测试完成${NC}"
        ;;
    5)
        read -p "请输入测试类名 (例如: AuthServiceTest): " testClass
        read -p "请输入测试方法名 (例如: testRegister_Success): " testMethod
        echo ""
        echo "=================================="
        echo "  运行测试: $testClass#$testMethod"
        echo "=================================="
        echo ""
        mvn test -Dtest="$testClass#$testMethod"
        echo ""
        echo -e "${GREEN}✓ 测试完成${NC}"
        ;;
    6)
        echo "=================================="
        echo "  生成测试覆盖率报告"
        echo "=================================="
        echo ""
        mvn clean test jacoco:report
        echo ""
        echo -e "${GREEN}✓ 覆盖率报告生成完成${NC}"
        echo ""
        echo "报告位置: target/site/jacoco/index.html"
        echo ""
        # 尝试打开报告（如果在支持的系统上）
        if command -v xdg-open &> /dev/null; then
            read -p "是否打开覆盖率报告? (y/n): " openReport
            if [ "$openReport" = "y" ]; then
                xdg-open target/site/jacoco/index.html
            fi
        elif command -v open &> /dev/null; then
            read -p "是否打开覆盖率报告? (y/n): " openReport
            if [ "$openReport" = "y" ]; then
                open target/site/jacoco/index.html
            fi
        fi
        ;;
    7)
        echo "=================================="
        echo "  清理并重新运行所有测试"
        echo "=================================="
        echo ""
        mvn clean test
        echo ""
        echo -e "${GREEN}✓ 所有测试完成${NC}"
        ;;
    q|Q)
        echo "退出..."
        exit 0
        ;;
    *)
        echo -e "${RED}无效的选项${NC}"
        exit 1
        ;;
esac

echo ""
echo "=================================="
echo "  测试运行完成"
echo "=================================="
