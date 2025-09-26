# TiGateway 文档站点 TypeScript 指南

本文档说明如何在 TiGateway 文档站点中使用 TypeScript 进行开发。

## 🎯 TypeScript 配置

### 项目结构

```
docs/
├── docusaurus.config.ts    # TypeScript 配置文件
├── sidebars.ts            # TypeScript 侧边栏配置
├── tsconfig.json          # TypeScript 编译配置
├── src/
│   ├── types/            # 类型定义
│   │   └── index.ts
│   ├── utils/            # 工具函数
│   │   └── index.ts
│   ├── components/       # TypeScript 组件
│   │   └── HomepageFeatures/
│   │       └── index.tsx
│   ├── pages/            # TypeScript 页面
│   │   └── index.tsx
│   └── module.d.ts       # 模块声明文件
└── package.json          # 包含 TypeScript 依赖
```

### 核心配置文件

#### tsconfig.json
```json
{
  "extends": "@docusaurus/tsconfig",
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@site/*": ["./src/*"]
    },
    "allowJs": true,
    "checkJs": false,
    "esModuleInterop": true,
    "allowSyntheticDefaultImports": true,
    "strict": true,
    "forceConsistentCasingInFileNames": true,
    "noFallthroughCasesInSwitch": true,
    "module": "esnext",
    "moduleResolution": "node",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "react-jsx"
  },
  "include": [
    "src/**/*",
    "docusaurus.config.ts",
    "sidebars.ts"
  ],
  "exclude": [
    "node_modules",
    "build"
  ]
}
```

#### docusaurus.config.ts
```typescript
import {themes as prismThemes} from 'prism-react-renderer';
import type {Config} from '@docusaurus/types';
import type * as Preset from '@docusaurus/preset-classic';

const config: Config = {
  // 配置内容...
  customFields: {
    tigateway: {
      version: '1.0.0',
      latestVersion: '1.0.0',
      repository: 'https://github.com/tigateway/tigateway',
      issues: 'https://github.com/tigateway/tigateway/issues',
      discussions: 'https://github.com/tigateway/tigateway/discussions',
    },
  },
};

export default config;
```

## 📝 类型定义

### 全局类型定义

在 `src/types/index.ts` 中定义全局类型：

```typescript
export interface FeatureItem {
  title: string;
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  description: React.ReactElement;
}

export interface NavbarItem {
  type: 'docSidebar' | 'localeDropdown' | 'docsVersionDropdown' | 'html';
  sidebarId?: string;
  position: 'left' | 'right';
  label: string;
  to?: string;
  href?: string;
}

export interface FooterLink {
  title: string;
  items: Array<{
    label: string;
    to?: string;
    href?: string;
  }>;
}
```

### 模块声明

在 `src/module.d.ts` 中声明模块类型：

```typescript
declare module '@site/static/img/*' {
  const content: React.ComponentType<React.ComponentProps<'svg'>>;
  export default content;
}

declare module '*.module.css' {
  const classes: {readonly [key: string]: string};
  export default classes;
}
```

## 🧩 组件开发

### TypeScript 组件示例

```typescript
import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';
import type {FeatureItem} from '@site/src/types';

interface FeatureProps {
  Svg: React.ComponentType<React.ComponentProps<'svg'>>;
  title: string;
  description: React.ReactElement;
}

function Feature({Svg, title, description}: FeatureProps) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  const features: FeatureItem[] = [
    // 特性数据...
  ];

  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {features.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
```

## 🛠️ 工具函数

### 类型安全的工具函数

```typescript
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';

export function useTiGatewayConfig() {
  const {siteConfig} = useDocusaurusContext();
  return siteConfig.customFields?.tigateway;
}

export function formatDate(date: string | Date): string {
  const d = new Date(date);
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

export function getDocUrl(docId: string): string {
  return `/docs/${docId}`;
}
```

## 🎨 样式模块

### CSS 模块类型

```typescript
import styles from './styles.module.css';

// styles 对象具有类型安全
const MyComponent: React.FC = () => {
  return (
    <div className={styles.container}>
      <h1 className={styles.title}>标题</h1>
    </div>
  );
};
```

## 📦 依赖管理

### package.json 中的 TypeScript 依赖

```json
{
  "dependencies": {
    "@docusaurus/core": "^3.4.0",
    "@docusaurus/preset-classic": "^3.4.0",
    "@docusaurus/theme-mermaid": "^3.4.0",
    "@docusaurus/theme-search-algolia": "^3.4.0",
    "@mdx-js/react": "^3.0.0",
    "clsx": "^2.1.0",
    "prism-react-renderer": "^2.3.0",
    "react": "^18.2.0",
    "react-dom": "^18.2.0"
  },
  "devDependencies": {
    "@docusaurus/module-type-aliases": "^3.4.0",
    "@docusaurus/tsconfig": "^3.4.0",
    "@docusaurus/types": "^3.4.0",
    "@types/react": "^18.2.0",
    "@types/react-dom": "^18.2.0",
    "typescript": "^5.3.0"
  }
}
```

## 🚀 开发命令

### TypeScript 相关命令

```bash
# 启动开发服务器（支持 TypeScript）
npm start

# 构建项目（TypeScript 编译）
npm run build

# 类型检查
npx tsc --noEmit

# 生成类型定义
npm run write-heading-ids
```

## 🔧 最佳实践

### 1. 类型定义
- 在 `src/types/` 目录中集中管理类型定义
- 使用接口定义复杂对象结构
- 为组件 props 定义明确的类型

### 2. 组件开发
- 使用函数组件和 TypeScript
- 为 props 定义接口
- 使用泛型提高代码复用性

### 3. 工具函数
- 在 `src/utils/` 目录中组织工具函数
- 为函数参数和返回值定义类型
- 使用类型守卫确保类型安全

### 4. 模块导入
- 使用 `@site/*` 路径别名
- 明确导入类型和值
- 使用 `import type` 导入纯类型

## 🐛 常见问题

### Q: 如何处理 SVG 导入？

A: 在 `src/module.d.ts` 中声明 SVG 模块类型：

```typescript
declare module '*.svg' {
  const content: React.ComponentType<React.ComponentProps<'svg'>>;
  export default content;
}
```

### Q: 如何处理 CSS 模块？

A: 在 `src/module.d.ts` 中声明 CSS 模块类型：

```typescript
declare module '*.module.css' {
  const classes: {readonly [key: string]: string};
  export default classes;
}
```

### Q: 如何扩展 Docusaurus 类型？

A: 使用模块声明扩展类型：

```typescript
declare module '@docusaurus/types' {
  interface Config {
    customFields?: {
      tigateway?: {
        version: string;
        repository: string;
      };
    };
  }
}
```

## 📚 相关资源

- [TypeScript 官方文档](https://www.typescriptlang.org/docs/)
- [React TypeScript 指南](https://react-typescript-cheatsheet.netlify.app/)
- [Docusaurus TypeScript 支持](https://docusaurus.io/docs/typescript-support)

---

**最后更新**: 2024-09-26  
**维护团队**: TiGateway 开发团队
