// TiGateway 文档站点类型定义

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

export interface BlogPost {
  slug: string;
  title: string;
  authors: string[];
  tags: string[];
  date: string;
  description?: string;
}

export interface DocItem {
  id: string;
  title: string;
  description?: string;
  sidebar_position?: number;
  tags?: string[];
}

// TiGateway 自定义配置类型
export interface TiGatewayConfig {
  version: string;
  latestVersion: string;
  repository: string;
  issues: string;
  discussions: string;
}
