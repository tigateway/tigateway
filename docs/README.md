# TiGateway 文档站点

这是 TiGateway 项目的官方文档站点，基于 Jekyll 构建，托管在 GitHub Pages 上。

## 📚 文档结构

```
docs/
├── _config.yml          # Jekyll 配置文件
├── _layouts/            # 页面布局模板
├── _includes/           # 可重用组件
├── assets/              # 静态资源
│   ├── css/            # 样式文件
│   └── js/             # JavaScript 文件
├── book/               # 用户指南 (32 个章节)
├── architecture/       # 架构设计文档
├── development/        # 开发指南
├── api/               # API 文档
├── deployment/        # 部署运维文档
├── examples/          # 示例教程
├── configuration/     # 配置文档
├── index.md          # 首页
├── 404.html          # 404 错误页面
└── Gemfile           # Ruby 依赖管理
```

## 🚀 本地开发

### 环境要求

- Ruby 3.1+
- Bundler
- Jekyll 4.3+

### 安装和运行

1. **安装 Ruby 依赖**
   ```bash
   cd docs
   bundle install
   ```

2. **启动本地服务器**
   ```bash
   bundle exec jekyll serve
   ```

3. **访问本地站点**
   打开浏览器访问 `http://localhost:4000`

### 开发命令

```bash
# 构建站点
bundle exec jekyll build

# 构建并启动服务器
bundle exec jekyll serve

# 构建并启动服务器（监听所有网络接口）
bundle exec jekyll serve --host 0.0.0.0

# 构建并启动服务器（生产模式）
JEKYLL_ENV=production bundle exec jekyll serve

# 清理构建缓存
bundle exec jekyll clean
```

## 📝 内容管理

### 添加新文档

1. **创建新文件**
   ```bash
   # 在相应目录下创建 .md 文件
   touch docs/examples/new-example.md
   ```

2. **添加 Front Matter**
   ```yaml
   ---
   layout: default
   title: 新示例
   description: 这是一个新的示例文档
   ---
   ```

3. **编写内容**
   使用 Markdown 语法编写文档内容

### 更新现有文档

直接编辑相应的 `.md` 文件即可。Jekyll 会自动检测文件变化并重新构建。

### 文档规范

- 使用 Markdown 格式
- 文件名使用小写字母和连字符
- 每个文档都应该有清晰的标题和描述
- 使用相对链接引用其他文档

## 🎨 样式和主题

### 自定义样式

样式文件位于 `assets/css/` 目录：

- `main.css` - 主要样式
- `syntax.css` - 代码语法高亮

### 主题配置

在 `_config.yml` 中配置主题和插件：

```yaml
theme: minima
plugins:
  - jekyll-feed
  - jekyll-sitemap
  - jekyll-seo-tag
```

## 🔧 功能特性

### 搜索功能

- 全站搜索
- 实时搜索建议
- 热门搜索推荐

### 代码高亮

- 支持多种编程语言
- 代码复制功能
- 行号显示

### 响应式设计

- 移动端适配
- 平板端优化
- 桌面端体验

### 导航功能

- 自动生成目录
- 面包屑导航
- 侧边栏导航

## 🚀 部署

### GitHub Pages 自动部署

项目配置了 GitHub Actions 工作流，当 `docs/` 目录下的文件发生变化时，会自动构建和部署到 GitHub Pages。

### 手动部署

1. **构建站点**
   ```bash
   cd docs
   bundle exec jekyll build
   ```

2. **部署到 GitHub Pages**
   ```bash
   # 使用 gh-pages 分支
   git subtree push --prefix docs/_site origin gh-pages
   ```

### 自定义域名

如果需要使用自定义域名，在 `docs/` 目录下创建 `CNAME` 文件：

```
docs.tigateway.cn
```

## 📊 性能优化

### 构建优化

- 使用 Jekyll 缓存
- 压缩静态资源
- 优化图片大小

### 加载优化

- 延迟加载图片
- 压缩 CSS 和 JavaScript
- 使用 CDN 加速

## 🔍 SEO 优化

### 元数据

每个页面都包含完整的 SEO 元数据：

- 页面标题
- 描述
- 关键词
- Open Graph 标签

### 结构化数据

使用 JSON-LD 格式添加结构化数据，提高搜索引擎理解。

## 🛠️ 故障排除

### 常见问题

1. **构建失败**
   - 检查 Ruby 版本
   - 更新依赖包
   - 查看错误日志

2. **样式不生效**
   - 清除浏览器缓存
   - 检查 CSS 文件路径
   - 验证 Jekyll 配置

3. **链接失效**
   - 检查文件路径
   - 验证相对链接
   - 更新导航配置

### 调试技巧

```bash
# 详细构建日志
bundle exec jekyll build --verbose

# 检查 Jekyll 配置
bundle exec jekyll doctor

# 验证 Markdown 语法
bundle exec jekyll build --trace
```

## 📞 支持

如果您在使用过程中遇到问题，请：

1. 查看 [故障排除指南](../examples/troubleshooting.md)
2. 提交 [Issue](https://github.com/tigateway/tigateway/issues)
3. 参与 [讨论](https://github.com/tigateway/tigateway/discussions)
4. 联系 [support@tigateway.cn](mailto:support@tigateway.cn)

## 📄 许可证

本文档遵循与 TiGateway 项目相同的许可证。详情请查看 [LICENSE](../LICENSE.md) 文件。

---

**最后更新**: 2024-09-26  
**维护团队**: TiGateway 开发团队