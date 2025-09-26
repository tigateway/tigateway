/**
 * Creating a sidebar enables you to:
 - create an ordered group of docs
 - render a sidebar for each doc of that group
 - provide next/previous navigation

 The sidebars can be generated from the filesystem, or explicitly defined here.

 Create as many sidebars as you want.
 */

import type {SidebarsConfig} from '@docusaurus/plugin-content-docs';

const sidebars: SidebarsConfig = {
  // By default, Docusaurus generates a sidebar from the docs folder structure
  tutorialSidebar: [
    {
      type: 'category',
      label: '第一部分：入门指南',
      items: [
        'introduction',
        'quick-start',
        'installation',
      ],
    },
    {
      type: 'category',
      label: '第二部分：核心概念',
      items: [
        'architecture',
        'routes-and-predicates',
        'filters',
        'global-filters',
      ],
    },
    {
      type: 'category',
      label: '第三部分：Kubernetes 集成',
      items: [
        'kubernetes-native',
        'crd-resources',
        'ingress-integration',
        'service-discovery',
      ],
    },
    {
      type: 'category',
      label: '第四部分：配置管理',
      items: [
        'configuration',
        'configmap-storage',
        'dynamic-configuration',
        'configuration-validation',
      ],
    },
    {
      type: 'category',
      label: '第五部分：高级功能',
      items: [
        'rate-limiting-and-circuit-breaker',
        'authentication-and-authorization',
        'monitoring-and-metrics',
        'logging-and-tracing',
      ],
    },
    {
      type: 'category',
      label: '第六部分：管理界面',
      items: [
        'admin-interface',
        'web-ui-guide',
        'rest-api-reference',
      ],
    },
    {
      type: 'category',
      label: '第七部分：运维指南',
      items: [
        'health-checks',
        'troubleshooting',
        'performance-tuning',
        'security-best-practices',
      ],
    },
    {
      type: 'category',
      label: '第八部分：开发指南',
      items: [
        'custom-components',
        'extension-development',
        'testing-guide',
      ],
    },
    {
      type: 'category',
      label: '第九部分：参考文档',
      items: [
        'configuration-properties',
        'api-reference',
        'faq',
      ],
    },
    {
      type: 'category',
      label: 'API 文档',
      items: [
        'api/rest-api',
        'api/crd-api',
        'api/websocket-api',
        'api/management-api',
      ],
    },
    {
      type: 'category',
      label: '架构设计',
      items: [
        'architecture/system-architecture',
        'architecture/module-design',
        'architecture/data-flow',
        'architecture/security',
      ],
    },
    {
      type: 'category',
      label: '开发指南',
      items: [
        'development/setup',
        'development/coding-standards',
        'development/testing',
        'development/debugging',
        'development/custom-components',
        'development/filter-factories',
        'development/predicate-factories',
        'development/spring-cloud-gateway-integration',
      ],
    },
    {
      type: 'category',
      label: '部署运维',
      items: [
        'deployment/kubernetes',
      ],
    },
    {
      type: 'category',
      label: '示例教程',
      items: [
        'examples/quick-start',
        'examples/basic-config',
        'examples/advanced-config',
        'examples/crd-basic-config',
        'examples/crd-advanced-config',
        'examples/troubleshooting',
      ],
    },
    {
      type: 'category',
      label: '配置文档',
      items: [
        'configuration/crd-configuration-design',
        'configuration/crd-resource-configuration',
        'configuration/crd-predicate-configuration',
        'configuration/crd-filter-configuration',
        'configuration/crd-typed-design',
      ],
    },
  ],
};

export default sidebars;
