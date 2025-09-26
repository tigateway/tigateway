# TiGateway 文档迁移指南

本文档说明如何从 Jekyll 迁移到 Docusaurus，以及迁移过程中的注意事项。

## 🎯 迁移概述

我们已经将 TiGateway 文档从 Jekyll 迁移到 Docusaurus，以获得更好的开发体验和更现代化的文档站点。

### 迁移优势

- **更好的开发体验**: 热重载、TypeScript 支持、现代构建工具
- **更丰富的功能**: 内置搜索、代码高亮、响应式设计
- **更好的 SEO**: 自动生成 sitemap、结构化数据
- **更简单的部署**: 与 GitHub Pages 无缝集成

## 📁 文件结构对比

### Jekyll 结构 (旧)
```
docs/
├── _config.yml          # Jekyll 配置
├── _layouts/            # 页面布局
├── _includes/           # 可重用组件
├── _sass/               # 样式文件
├── assets/              # 静态资源
├── book/                # 用户指南
├── api/                 # API 文档
├── architecture/        # 架构文档
├── development/         # 开发指南
├── deployment/          # 部署文档
├── examples/            # 示例教程
├── configuration/       # 配置文档
├── index.md             # 首页
├── 404.html             # 404 页面
└── Gemfile              # Ruby 依赖
```

### Docusaurus 结构 (新)
```
docs/
├── docusaurus.config.js # Docusaurus 配置
├── sidebars.js          # 侧边栏配置
├── package.json         # Node.js 依赖
├── tsconfig.json        # TypeScript 配置
├── src/                 # 源代码
│   ├── pages/          # 页面组件
│   ├── components/     # 可重用组件
│   └── css/            # 样式文件
├── static/             # 静态资源
│   └── img/            # 图片资源
├── docs/               # 文档内容
│   ├── api/            # API 文档
│   ├── architecture/   # 架构文档
│   ├── development/    # 开发指南
│   ├── deployment/     # 部署文档
│   ├── examples/       # 示例教程
│   └── configuration/  # 配置文档
└── blog/               # 博客文章
```

## 🔄 迁移步骤

### 1. 安装依赖

```bash
cd docs
npm install
```

### 2. 启动开发服务器

```bash
npm start
```

### 3. 构建生产版本

```bash
npm run build
```

### 4. 清理旧文件 (可选)

```bash
./cleanup-jekyll.sh
```

## 📝 内容迁移

### Front Matter 变化

#### Jekyll 格式
```yaml
---
layout: default
title: 页面标题
permalink: /path/to/page
---
```

#### Docusaurus 格式
```yaml
---
sidebar_position: 1
title: 页面标题
description: 页面描述
---
```

### 链接语法变化

#### Jekyll 格式
```markdown
[链接文本]({{ '/path/to/page' | relative_url }})
```

#### Docusaurus 格式
```markdown
[链接文本](/path/to/page)
```

### 图片引用变化

#### Jekyll 格式
```markdown
![图片]({{ '/assets/img/image.png' | relative_url }})
```

#### Docusaurus 格式
```markdown
![图片](/img/image.png)
```

## 🎨 样式迁移

### CSS 变量

Docusaurus 使用 CSS 变量来管理主题：

```css
:root {
  --ifm-color-primary: #2e8555;
  --ifm-color-primary-dark: #29784c;
  --ifm-color-primary-darker: #277148;
  --ifm-color-primary-darkest: #205d3b;
  --ifm-color-primary-light: #33925d;
  --ifm-color-primary-lighter: #359962;
  --ifm-color-primary-lightest: #3cad6e;
}
```

### 自定义样式

在 `src/css/custom.css` 中添加自定义样式：

```css
/* 自定义样式 */
.hero__title {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}
```

## 🔧 配置迁移

### 导航配置

在 `docusaurus.config.js` 中配置导航：

```javascript
navbar: {
  title: 'TiGateway',
  logo: {
    alt: 'TiGateway Logo',
    src: 'img/logo.svg',
  },
  items: [
    {
      type: 'docSidebar',
      sidebarId: 'tutorialSidebar',
      position: 'left',
      label: '文档',
    },
    {
      to: '/blog',
      label: '博客',
      position: 'left'
    },
  ],
},
```

### 侧边栏配置

在 `sidebars.js` 中配置侧边栏：

```javascript
const sidebars = {
  tutorialSidebar: [
    {
      type: 'category',
      label: '开始使用',
      items: [
        'introduction',
        'quick-start',
        'installation',
        'architecture',
      ],
    },
    // 更多配置...
  ],
};
```

## 🚀 部署迁移

### GitHub Pages 配置

1. **启用 GitHub Pages**
   - 进入仓库设置
   - 选择 "Pages" 部分
   - 在 "Source" 下选择 "GitHub Actions"

2. **工作流配置**
   已创建 `.github/workflows/docs.yml` 文件，自动处理构建和部署。

3. **自定义域名** (可选)
   在 `static/` 目录下创建 `CNAME` 文件：
   ```
   docs.tigateway.cn
   ```

## 🐛 常见问题

### Q: 如何添加新的文档页面？

A: 在 `docs/` 目录下创建新的 `.md` 文件，并在 `sidebars.js` 中添加相应的配置。

### Q: 如何自定义主题？

A: 在 `src/css/custom.css` 中添加自定义样式，或使用 Docusaurus 的主题系统。

### Q: 如何添加搜索功能？

A: 在 `docusaurus.config.js` 中配置 Algolia 搜索：

```javascript
algolia: {
  appId: 'YOUR_APP_ID',
  apiKey: 'YOUR_SEARCH_API_KEY',
  indexName: 'tigateway',
}
```

### Q: 如何添加多语言支持？

A: 在 `docusaurus.config.js` 中配置 i18n：

```javascript
i18n: {
  defaultLocale: 'zh-Hans',
  locales: ['zh-Hans', 'en'],
}
```

## 📚 相关资源

- [Docusaurus 官方文档](https://docusaurus.io/docs)
- [Docusaurus 配置参考](https://docusaurus.io/docs/api/docusaurus-config)
- [Docusaurus 主题系统](https://docusaurus.io/docs/styling-layout)
- [Docusaurus 部署指南](https://docusaurus.io/docs/deployment)

## 🤝 支持

如果您在迁移过程中遇到问题：

1. 查看 [Docusaurus 文档](https://docusaurus.io/docs)
2. 提交 [Issue](https://github.com/tigateway/tigateway/issues)
3. 参与 [讨论](https://github.com/tigateway/tigateway/discussions)
4. 联系 [support@tigateway.cn](mailto:support@tigateway.cn)

---

**迁移完成时间**: 2024-09-26  
**维护团队**: TiGateway 开发团队
