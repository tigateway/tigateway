#!/bin/bash

# 测试 Jekyll 构建脚本

echo "开始测试 Jekyll 构建..."

# 检查 Ruby 版本
echo "Ruby 版本:"
ruby --version

# 检查 Bundler 版本
echo "Bundler 版本:"
bundle --version

# 安装依赖
echo "安装 Jekyll 依赖..."
bundle install

# 构建站点
echo "构建 Jekyll 站点..."
bundle exec jekyll build --verbose

# 检查构建结果
echo "检查构建结果..."
if [ -d "_site" ]; then
    echo "✅ _site 目录存在"
    
    if [ -f "_site/index.html" ]; then
        echo "✅ index.html 文件存在"
        echo "文件大小: $(wc -c < _site/index.html) 字节"
        echo "文件前几行:"
        head -10 _site/index.html
    else
        echo "❌ index.html 文件不存在"
        echo "_site 目录内容:"
        ls -la _site/
    fi
else
    echo "❌ _site 目录不存在"
fi

echo "测试完成"
