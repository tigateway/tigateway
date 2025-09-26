#!/bin/bash

# TiGateway 文档迁移脚本 - 清理 Jekyll 文件
# 此脚本将移除不再需要的 Jekyll 相关文件

echo "🧹 开始清理 Jekyll 文件..."

# 备份重要文件
echo "📦 备份重要文件..."
mkdir -p backup
cp -r _config.yml _layouts _includes _sass assets backup/ 2>/dev/null || true

# 移除 Jekyll 特定文件
echo "🗑️  移除 Jekyll 特定文件..."
rm -f _config.yml
rm -f Gemfile
rm -f Gemfile.lock
rm -f .nojekyll
rm -f GITHUB_PAGES_SETUP.md

# 移除 Jekyll 目录
echo "📁 移除 Jekyll 目录..."
rm -rf _layouts
rm -rf _includes
rm -rf _sass
rm -rf assets

# 移除旧的文档文件（保留内容，但移除 Jekyll 格式）
echo "📄 处理文档文件..."
find . -name "*.md" -path "./book/*" -exec rm {} \; 2>/dev/null || true
find . -path "./api/*" -name "*.md" -exec rm {} \; 2>/dev/null || true
find . -path "./architecture/*" -name "*.md" -exec rm {} \; 2>/dev/null || true
find . -path "./development/*" -name "*.md" -exec rm {} \; 2>/dev/null || true
find . -path "./deployment/*" -name "*.md" -exec rm {} \; 2>/dev/null || true
find . -path "./examples/*" -name "*.md" -exec rm {} \; 2>/dev/null || true
find . -path "./configuration/*" -name "*.md" -exec rm {} \; 2>/dev/null || true

# 移除其他 Jekyll 文件
rm -f 404.html
rm -f test-build.sh

echo "✅ Jekyll 文件清理完成！"
echo "📁 备份文件保存在 backup/ 目录中"
echo "🚀 现在可以运行 'npm install && npm start' 启动 Docusaurus 开发服务器"
