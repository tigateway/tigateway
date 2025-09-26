# TiGateway 文档站点

这是 TiGateway 项目的官方文档站点，基于 Docusaurus 构建，托管在 GitHub Pages 上。

## 📚 文档结构

```
docs/
├── docusaurus.config.js    # Docusaurus 配置文件
├── sidebars.js            # 侧边栏配置
├── package.json           # 项目依赖
├── tsconfig.json          # TypeScript 配置
├── src/                   # 源代码
│   ├── pages/            # 页面组件
│   ├── components/       # 可重用组件
│   └── css/              # 样式文件
├── static/               # 静态资源
│   └── img/              # 图片资源
├── docs/                 # 文档内容
│   ├── api/              # API 文档
│   ├── architecture/     # 架构文档
│   ├── development/      # 开发指南
│   ├── deployment/       # 部署文档
│   ├── examples/         # 示例教程
│   └── configuration/    # 配置文档
└── blog/                 # 博客文章
```

## 🚀 本地开发

### 环境要求

- Node.js 18+
- npm 或 yarn

### 安装和运行

1. **安装依赖**
   ```bash
   cd docs
   npm install
   ```

2. **启动开发服务器**
   ```bash
   npm start
   ```

3. **访问本地站点**
   打开浏览器访问 `http://localhost:3000`

### 开发命令

```bash
# 启动开发服务器
npm start

# 构建生产版本
npm run build

# 启动生产服务器
npm run serve

# 清理构建缓存
npm run clear

# 生成类型定义
npm run write-heading-ids
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
   sidebar_position: 1
   title: 新示例
   description: 这是一个新的示例文档
   ---
   ```

3. **编写内容**
   使用 Markdown 语法编写文档内容

### 更新现有文档

直接编辑相应的 `.md` 文件即可。Docusaurus 会自动检测文件变化并重新构建。

### 文档规范

- 使用 Markdown 格式
- 文件名使用小写字母和连字符
- 每个文档都应该有清晰的标题和描述
- 使用相对链接引用其他文档

## 🎨 样式和主题

### 自定义样式

样式文件位于 `src/css/custom.css`：

- 全局样式覆盖
- 主题变量自定义
- 响应式设计

### 主题配置

在 `docusaurus.config.js` 中配置主题和插件：

```javascript
themeConfig: {
  navbar: {
    title: 'TiGateway',
    logo: {
      alt: 'TiGateway Logo',
      src: 'img/logo.svg',
    },
    items: [
      // 导航项配置
    ],
  },
  // 更多配置...
}
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
   npm run build
   ```

2. **部署到 GitHub Pages**
   ```bash
   npm run deploy
   ```

### 自定义域名

如果需要使用自定义域名，在 `static/` 目录下创建 `CNAME` 文件：

```
docs.tigateway.cn
```

## 📊 性能优化

### 构建优化

- 使用 Docusaurus 缓存
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
   - 检查 Node.js 版本
   - 更新依赖包
   - 查看错误日志

2. **样式不生效**
   - 清除浏览器缓存
   - 检查 CSS 文件路径
   - 验证 Docusaurus 配置

3. **链接失效**
   - 检查文件路径
   - 验证相对链接
   - 更新导航配置

### 调试技巧

```bash
# 详细构建日志
npm run build -- --verbose

# 检查 Docusaurus 配置
npm run docusaurus -- --help

# 验证 Markdown 语法
npm run build -- --trace
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