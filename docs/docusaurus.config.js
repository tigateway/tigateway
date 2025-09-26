const {themes: prismThemes} = require('prism-react-renderer');

const config = {
  title: 'TiGateway',
  tagline: '基于 Spring Cloud Gateway 的 Kubernetes 原生 API 网关',
  favicon: 'img/favicon.ico',

  // Set the production url of your site here
  url: 'https://tigateway.github.io',
  // Set the /<baseUrl>/ pathname under which your site is served
  // For GitHub pages deployment, it is often '/<projectName>/'
  // baseUrl is automatically injected by actions/configure-pages@v4
  baseUrl: '/',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'tigateway', // Usually your GitHub org/user name.
  projectName: 'tigateway', // Usually your repo name.
  deploymentBranch: 'gh-pages', // Branch that GitHub Pages will deploy from.
  trailingSlash: false, // GitHub Pages doesn't support trailing slashes.

  onBrokenLinks: 'warn',
  markdown: {
    hooks: {
      onBrokenMarkdownLinks: 'warn',
    },
    mermaid: true,
  },

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
          authorsMapPath: 'authors.yml',
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
          {
            to: '/blog',
            label: '博客',
            position: 'left'
          },
          {
            type: 'localeDropdown',
            position: 'right',
          },
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
                to: '/docs/quick-start',
              },
              {
                label: '安装指南',
                to: '/docs/installation',
              },
              {
                label: 'API 参考',
                to: '/docs/api/rest-api',
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
              {
                label: '讨论区',
                href: 'https://github.com/tigateway/tigateway/discussions',
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
                label: '更新日志',
                to: '/docs/changelog',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} TiGateway Team. Built with Docusaurus.`,
      },
      prism: {
        theme: prismThemes.github,
        darkTheme: prismThemes.dracula,
        additionalLanguages: ['java', 'yaml', 'bash', 'json'],
      },
      algolia: {
        // The application ID provided by Algolia
        appId: 'YOUR_APP_ID',
        // Public API key: it is safe to commit it
        apiKey: 'YOUR_SEARCH_API_KEY',
        indexName: 'tigateway',
        // Optional: see doc section below
        contextualSearch: true,
        // Optional: Specify domains where the navigation should occur through window.location instead on history.push. Useful when our Algolia config crawls multiple documentation sites and we want to navigate with window.location.href to them.
        externalUrlRegex: 'external\\.com|domain\\.com',
        // Optional: Replace parts of the item URLs from Algolia. Useful when using the same search index for multiple deployments using a different baseUrl. You can use regexp or string in the `from` param. For example: localhost:3000 vs myCompany.com/docs
        replaceSearchResultPathname: {
          from: '/docs/', // or as RegExp: /\/docs\//
          to: '/',
        },
        // Optional: Algolia search parameters
        searchParameters: {},
        // Optional: path for search page that enabled by default (`false` to disable it)
        searchPagePath: 'search',
      },
      mermaid: {
        theme: {light: 'neutral', dark: 'dark'},
      },
    },

  themes: ['@docusaurus/theme-mermaid'],

  plugins: [],

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
