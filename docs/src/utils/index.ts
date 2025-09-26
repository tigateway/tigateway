// TiGateway 文档站点工具函数

import useDocusaurusContext from '@docusaurus/useDocusaurusContext';

/**
 * 获取 TiGateway 自定义配置
 */
export function useTiGatewayConfig() {
  const {siteConfig} = useDocusaurusContext();
  return siteConfig.customFields?.tigateway;
}

/**
 * 格式化日期
 */
export function formatDate(date: string | Date): string {
  const d = new Date(date);
  return d.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
}

/**
 * 生成文档链接
 */
export function getDocUrl(docId: string): string {
  return `/docs/${docId}`;
}

/**
 * 生成 API 文档链接
 */
export function getApiDocUrl(apiId: string): string {
  return `/docs/api/${apiId}`;
}

/**
 * 检查是否为外部链接
 */
export function isExternalUrl(url: string): boolean {
  return url.startsWith('http://') || url.startsWith('https://');
}

/**
 * 获取 GitHub 链接
 */
export function getGitHubUrl(path: string = ''): string {
  const config = useTiGatewayConfig();
  return `${config?.repository || 'https://github.com/tigateway/tigateway'}${path}`;
}

/**
 * 获取问题反馈链接
 */
export function getIssuesUrl(): string {
  const config = useTiGatewayConfig();
  return config?.issues || 'https://github.com/tigateway/tigateway/issues';
}

/**
 * 获取讨论区链接
 */
export function getDiscussionsUrl(): string {
  const config = useTiGatewayConfig();
  return config?.discussions || 'https://github.com/tigateway/tigateway/discussions';
}

/**
 * 生成面包屑导航
 */
export function generateBreadcrumbs(path: string): Array<{label: string; href?: string}> {
  const segments = path.split('/').filter(Boolean);
  const breadcrumbs = [{label: '首页', href: '/'}];
  
  let currentPath = '';
  segments.forEach((segment, index) => {
    currentPath += `/${segment}`;
    const isLast = index === segments.length - 1;
    breadcrumbs.push({
      label: segment,
      href: isLast ? undefined : currentPath,
    });
  });
  
  return breadcrumbs;
}

/**
 * 截断文本
 */
export function truncateText(text: string, maxLength: number): string {
  if (text.length <= maxLength) {
    return text;
  }
  return text.slice(0, maxLength) + '...';
}

/**
 * 高亮搜索关键词
 */
export function highlightSearchTerm(text: string, searchTerm: string): string {
  if (!searchTerm) return text;
  
  const regex = new RegExp(`(${searchTerm})`, 'gi');
  return text.replace(regex, '<mark>$1</mark>');
}
