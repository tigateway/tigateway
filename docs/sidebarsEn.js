/**
 * English sidebar configuration for TiGateway documentation
 * Creating a sidebar enables you to:
 * - create an ordered group of docs
 * - render a sidebar for each doc of that group
 * - provide next/previous navigation
 *
 * The sidebars can be generated from the filesystem, or explicitly defined here.
 *
 * Create as many sidebars as you want.
 */

const sidebars = {
  // By default, Docusaurus generates a sidebar from the docs folder structure
  tutorialSidebar: [
    {
      type: 'category',
      label: 'Part I: Getting Started',
      items: [
        'introduction',
        'quick-start',
        'installation',
      ],
    },
    {
      type: 'category',
      label: 'Part II: Core Concepts',
      items: [
        'architecture',
        'routes-and-predicates',
        'filters',
        'global-filters',
      ],
    },
    {
      type: 'category',
      label: 'Part III: Kubernetes Integration',
      items: [
        'kubernetes-native',
        'crd-resources',
        'ingress-integration',
        'service-discovery',
      ],
    },
    {
      type: 'category',
      label: 'Part IV: Configuration Management',
      items: [
        'configuration',
        'configmap-storage',
        'dynamic-configuration',
        'configuration-validation',
      ],
    },
    {
      type: 'category',
      label: 'Part V: Advanced Features',
      items: [
        'rate-limiting-and-circuit-breaker',
        'authentication-and-authorization',
        'monitoring-and-metrics',
        'logging-and-tracing',
      ],
    },
    {
      type: 'category',
      label: 'Part VI: Administration',
      items: [
        'administration/admin-interface',
        'web-ui-guide',
        'rest-api-reference',
      ],
    },
    {
      type: 'category',
      label: 'Part VII: Operations',
      items: [
        'health-checks',
        'troubleshooting',
        'performance-tuning',
        'security-best-practices',
      ],
    },
    {
      type: 'category',
      label: 'Part VIII: Development',
      items: [
        'custom-components',
        'extension-development',
        'testing-guide',
      ],
    },
    {
      type: 'category',
      label: 'Part IX: Reference',
      items: [
        'configuration-properties',
        'api-reference',
        'faq',
        'changelog',
      ],
    },
    {
      type: 'category',
      label: 'API Documentation',
      items: [
        'api/rest-api',
        'api/crd-api',
        'api/websocket-api',
        'api/admin-api',
      ],
    },
    {
      type: 'category',
      label: 'Architecture Design',
      items: [
        'architecture/system-architecture',
        'architecture/module-design',
        'architecture/data-flow',
        'architecture/security',
      ],
    },
    {
      type: 'category',
      label: 'Development Guide',
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
      label: 'Deployment & Operations',
      items: [
        'deployment/kubernetes',
        'deployment/docker',
        'deployment/helm',
        'deployment/monitoring',
      ],
    },
    {
      type: 'category',
      label: 'Examples & Tutorials',
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
      label: 'Configuration Documentation',
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

module.exports = sidebars;
