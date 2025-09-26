# GitHub Pages 配置指南

## 🎯 概述

本文档说明如何为 TiGateway 项目配置和使用 GitHub Pages 功能，让文档站点能够自动构建和部署。

## 📋 配置步骤

### 1. 启用 GitHub Pages

1. 进入 GitHub 仓库设置页面
2. 滚动到 "Pages" 部分
3. 在 "Source" 下选择 "GitHub Actions"
4. 保存设置

### 2. 配置构建源

GitHub Pages 将使用以下配置：

- **源目录**: `docs/`
- **构建工具**: Jekyll
- **部署分支**: `gh-pages` (自动生成)
- **工作流文件**: `.github/workflows/docs.yml`

### 3. 验证配置

配置完成后，GitHub Actions 会自动：

1. 检测 `docs/` 目录的变化
2. 安装 Ruby 和 Jekyll 依赖
3. 构建静态站点
4. 部署到 GitHub Pages

## 🚀 访问文档站点

### 默认 URL

```
https://[用户名].github.io/[仓库名]
```

例如：
```
https://tigateway.github.io/tigateway
```

### 自定义域名 (可选)

如果需要使用自定义域名：

1. 在 `docs/` 目录下创建 `CNAME` 文件：
   ```
   docs.tigateway.cn
   ```

2. 在 DNS 提供商处配置 CNAME 记录：
   ```
   docs.tigateway.cn CNAME [用户名].github.io
   ```

## 🔧 本地开发

### 环境要求

- Ruby 3.1+
- Bundler
- Jekyll 4.3+

### 安装和运行

```bash
# 进入文档目录
cd docs

# 安装依赖
bundle install

# 启动本地服务器
bundle exec jekyll serve

# 访问本地站点
open http://localhost:4000
```

### 开发命令

```bash
# 构建站点
bundle exec jekyll build

# 清理缓存
bundle exec jekyll clean

# 检查配置
bundle exec jekyll doctor
```

## 📁 文件结构

```
docs/
├── _config.yml          # Jekyll 配置
├── _layouts/            # 页面布局
├── _includes/           # 可重用组件
├── assets/              # 静态资源
│   ├── css/            # 样式文件
│   └── js/             # JavaScript 文件
├── book/               # 用户指南
├── architecture/       # 架构文档
├── development/        # 开发指南
├── api/               # API 文档
├── deployment/        # 部署文档
├── examples/          # 示例教程
├── configuration/     # 配置文档
├── index.md          # 首页
├── 404.html          # 404 页面
├── Gemfile           # Ruby 依赖
└── .nojekyll         # 禁用 Jekyll 处理
```

## 🎨 功能特性

### 响应式设计
- 移动端适配
- 平板端优化
- 桌面端体验

### 搜索功能
- 全站搜索
- 实时建议
- 热门搜索

### 代码高亮
- 多语言支持
- 复制功能
- 行号显示

### 导航功能
- 自动目录
- 面包屑导航
- 侧边栏导航

### 主题支持
- 深色/浅色模式
- 自定义样式
- 打印优化

## 🔄 工作流程

### 自动部署流程

1. **代码推送**
   ```bash
   git add docs/
   git commit -m "docs: 更新文档"
   git push origin main
   ```

2. **自动构建**
   - GitHub Actions 检测到变化
   - 安装 Ruby 和 Jekyll 依赖
   - 构建静态站点

3. **自动部署**
   - 部署到 `gh-pages` 分支
   - 更新 GitHub Pages 站点
   - 发送部署通知

### 手动部署

如果需要手动部署：

```bash
# 构建站点
cd docs
bundle exec jekyll build

# 部署到 gh-pages 分支
git subtree push --prefix docs/_site origin gh-pages
```

## 🛠️ 故障排除

### 常见问题

1. **构建失败**
   - 检查 Ruby 版本 (需要 3.1+)
   - 更新依赖包: `bundle update`
   - 查看 GitHub Actions 日志

2. **样式不生效**
   - 清除浏览器缓存
   - 检查 CSS 文件路径
   - 验证 Jekyll 配置

3. **链接失效**
   - 检查文件路径
   - 验证相对链接
   - 更新导航配置

4. **部署失败**
   - 检查 GitHub Actions 权限
   - 验证工作流配置
   - 查看部署日志

### 调试技巧

```bash
# 详细构建日志
bundle exec jekyll build --verbose

# 检查 Jekyll 配置
bundle exec jekyll doctor

# 验证 Markdown 语法
bundle exec jekyll build --trace

# 检查 GitHub Actions 状态
gh run list --workflow=docs.yml
```

## 📊 性能优化

### 构建优化

- 使用 Jekyll 缓存
- 压缩静态资源
- 优化图片大小
- 启用 Gzip 压缩

### 加载优化

- 延迟加载图片
- 压缩 CSS 和 JavaScript
- 使用 CDN 加速
- 启用浏览器缓存

## 🔍 SEO 优化

### 元数据配置

每个页面都包含完整的 SEO 元数据：

```yaml
---
layout: default
title: 页面标题
description: 页面描述
keywords: 关键词1, 关键词2
---
```

### 结构化数据

使用 JSON-LD 格式添加结构化数据，提高搜索引擎理解。

## 📞 支持

如果您在配置过程中遇到问题：

1. 查看 [故障排除指南](../examples/troubleshooting.md)
2. 提交 [Issue](https://github.com/tigateway/tigateway/issues)
3. 参与 [讨论](https://github.com/tigateway/tigateway/discussions)
4. 联系 [support@tigateway.cn](mailto:support@tigateway.cn)

## 📄 相关文档

- [Jekyll 官方文档](https://jekyllrb.com/docs/)
- [GitHub Pages 文档](https://docs.github.com/en/pages)
- [GitHub Actions 文档](https://docs.github.com/en/actions)
- [TiGateway 文档站点](../README.md)

---

**最后更新**: 2024-09-26  
**维护团队**: TiGateway 开发团队
