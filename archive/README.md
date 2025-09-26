# 文档归档目录

本目录包含已归档的旧文档和配置文件。

## 目录结构

### jekyll-docs/
包含从 Jekyll 迁移到 Docusaurus 之前的原始文档文件：

- `_config.yml` - Jekyll 配置文件
- `_includes/` - Jekyll 包含文件
- `_layouts/` - Jekyll 布局文件
- `_sass/` - Jekyll 样式文件
- `404.html` - 自定义 404 页面
- `assets/` - 静态资源文件
- `Gemfile` - Ruby 依赖文件
- `index.md` - Jekyll 首页
- `book/` - 原始书籍文档
- `api/` - API 文档
- `architecture/` - 架构文档
- `configuration/` - 配置文档
- `deployment/` - 部署文档
- `development/` - 开发文档
- `examples/` - 示例文档
- `MIGRATION_*.md` - 迁移相关文档
- `GITHUB_PAGES_SETUP.md` - GitHub Pages 设置文档
- `cleanup-jekyll.sh` - Jekyll 清理脚本

## 迁移状态

✅ **已完成**：所有文档已成功迁移到 Docusaurus 格式
- 文档内容已迁移到 `docs/` 目录
- 侧边栏配置已更新为 TypeScript 版本
- 主题和样式已适配 Docusaurus

## 注意事项

- 这些文件仅作为历史记录保留
- 如需参考原始内容，请查看 `docs/` 目录中的新版本
- 不建议直接使用这些归档文件，因为它们可能包含过时的配置

## 清理建议

如果确认不再需要这些归档文件，可以安全删除整个 `archive/` 目录。
