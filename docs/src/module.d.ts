// Docusaurus 模块声明文件

declare module '@theme/Layout' {
  import { ComponentType, ReactNode } from 'react';
  interface LayoutProps {
    title?: string;
    description?: string;
    children: ReactNode;
  }
  const Layout: ComponentType<LayoutProps>;
  export default Layout;
}

declare module '@docusaurus/Link' {
  import { ComponentType, AnchorHTMLAttributes } from 'react';
  interface LinkProps extends AnchorHTMLAttributes<HTMLAnchorElement> {
    to?: string;
    href?: string;
  }
  const Link: ComponentType<LinkProps>;
  export default Link;
}

declare module '@docusaurus/useDocusaurusContext' {
  interface DocusaurusContext {
    siteConfig: {
      title: string;
      tagline: string;
    };
  }
  const useDocusaurusContext: () => DocusaurusContext;
  export default useDocusaurusContext;
}

declare module '@site/static/img/*' {
  const content: React.ComponentType<React.ComponentProps<'svg'>>;
  export default content;
}

declare module '*.module.css' {
  const classes: {readonly [key: string]: string};
  export default classes;
}

declare module '*.css' {
  const content: string;
  export default content;
}

declare module '*.svg' {
  const content: React.ComponentType<React.ComponentProps<'svg'>>;
  export default content;
}

declare module '*.png' {
  const content: string;
  export default content;
}

declare module '*.jpg' {
  const content: string;
  export default content;
}

declare module '*.jpeg' {
  const content: string;
  export default content;
}

declare module '*.gif' {
  const content: string;
  export default content;
}

declare module '*.webp' {
  const content: string;
  export default content;
}
