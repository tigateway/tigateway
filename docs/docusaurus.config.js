const config = {
  title: 'TiGateway',
  tagline: '基于 Spring Cloud Gateway 的 Kubernetes 原生 API 网关',
  favicon: 'img/favicon.ico',

  // Set the production url of your site here
  url: 'https://tigateway.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  baseUrl: '/tigateway/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'tigateway', // Usually your GitHub org/user name.
  projectName: 'tigateway', // Usually your repo name.

  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',

  // Even if you don't use internationalization, you can use this field to set
  // useful metadata like html lang. For example, if your site is Chinese, you
  // may want to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'zh-Hans',
    locales: ['zh-Hans', 'en'],
    localeConfigs: {
      'zh-Hans': {
        label: '简体中文',
      },
      en: {
        label: 'English',
      },
    },
  },

  presets: [
    [
      'classic',
      {
        docs: {
          sidebarPath: './sidebars.js',
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl: 'https://github.com/tigateway/tigateway/tree/main/docs/',
          showLastUpdateTime: true,
          showLastUpdateAuthor: true,
        },
        blog: {
          showReadingTime: true,
          // Please change this to your repo.
          // Remove this to remove the "edit this page" links.
          editUrl: 'https://github.com/tigateway/tigateway/tree/main/docs/',
        },
        theme: {
          customCss: './src/css/custom.css',
        },
      },
    ],
  ],

  themeConfig: {
    // Replace with your project's social card
    image: 'img/tigateway-social-card.jpg',
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
        {to: '/blog', label: '博客', position: 'left'},
        {
          href: 'https://github.com/tigateway/tigateway',
          label: 'GitHub',
          position: 'right',
        },
      ],
    },
    footer: {
      style: 'dark',
      links: [
        {
          title: '文档',
          items: [
            {
              label: '快速开始',
              to: '/docs/introduction',
            },
            {
              label: '架构概述',
              to: '/docs/architecture',
            },
          ],
        },
        {
          title: '社区',
          items: [
            {
              label: 'GitHub',
              href: 'https://github.com/tigateway/tigateway',
            },
            {
              label: '问题反馈',
              href: 'https://github.com/tigateway/tigateway/issues',
            },
          ],
        },
        {
          title: '更多',
          items: [
            {
              label: '博客',
              to: '/blog',
            },
            {
              label: 'GitHub',
              href: 'https://github.com/tigateway/tigateway',
            },
          ],
        },
      ],
      copyright: `Copyright © ${new Date().getFullYear()} TiGateway Team. Built with Docusaurus.`,
    },
    prism: {
      theme: require('prism-react-renderer/themes/github'),
      darkTheme: require('prism-react-renderer/themes/dracula'),
      additionalLanguages: ['java', 'yaml', 'bash', 'json'],
    },
  },

  // Custom fields for TiGateway
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

module.exports = config;