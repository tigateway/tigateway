import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '../components/HomepageFeatures';

import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <div className={styles.heroContent}>
          <div className={styles.heroText}>
            <h1 className="hero__title">{siteConfig.title}</h1>
            <p className="hero__subtitle">{siteConfig.tagline}</p>
            <p className={styles.heroDescription}>
              专为云原生环境设计的下一代 API 网关，提供完整的微服务治理、安全防护和可观测性能力
            </p>
            <div className={styles.buttons}>
              <Link
                className="button button--secondary button--lg"
                to="/docs/introduction">
                🚀 开始使用
              </Link>
              <Link
                className="button button--outline button--lg"
                to="/docs/quick-start">
                📖 快速开始
              </Link>
            </div>
            <div className={styles.heroStats}>
              <div className={styles.statItem}>
                <div className={styles.statNumber}>99.9%</div>
                <div className={styles.statLabel}>可用性</div>
              </div>
              <div className={styles.statItem}>
                <div className={styles.statNumber}>&lt;10ms</div>
                <div className={styles.statLabel}>延迟</div>
              </div>
              <div className={styles.statItem}>
                <div className={styles.statNumber}>10K+</div>
                <div className={styles.statLabel}>QPS</div>
              </div>
            </div>
          </div>
          <div className={styles.heroVisual}>
            <div className={styles.architectureDiagram}>
              <div className={styles.diagramNode}>Kubernetes</div>
              <div className={styles.diagramArrow}>→</div>
              <div className={styles.diagramNode}>TiGateway</div>
              <div className={styles.diagramArrow}>→</div>
              <div className={styles.diagramNode}>微服务</div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}

function HomepageCapabilities() {
  return (
    <section className={styles.capabilities}>
      <div className="container">
        <div className="text--center margin-bottom--lg">
          <h2>核心能力</h2>
          <p>为现代微服务架构提供完整的网关解决方案</p>
        </div>
        <div className="row">
          <div className="col col--4">
            <div className={styles.capabilityCard}>
              <div className={styles.capabilityIcon}>🛡️</div>
              <h3>安全防护</h3>
              <ul>
                <li>JWT/OAuth2 认证</li>
                <li>RBAC 权限控制</li>
                <li>API 限流熔断</li>
                <li>IP 白名单/黑名单</li>
              </ul>
            </div>
          </div>
          <div className="col col--4">
            <div className={styles.capabilityCard}>
              <div className={styles.capabilityIcon}>⚡</div>
              <h3>高性能</h3>
              <ul>
                <li>响应式架构</li>
                <li>智能负载均衡</li>
                <li>连接池管理</li>
                <li>缓存优化</li>
              </ul>
            </div>
          </div>
          <div className="col col--4">
            <div className={styles.capabilityCard}>
              <div className={styles.capabilityIcon}>📊</div>
              <h3>可观测性</h3>
              <ul>
                <li>Prometheus 指标</li>
                <li>分布式链路追踪</li>
                <li>结构化日志</li>
                <li>Grafana 仪表板</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function HomepageUseCases() {
  return (
    <section className={styles.useCases}>
      <div className="container">
        <div className="text--center margin-bottom--lg">
          <h2>应用场景</h2>
          <p>适用于各种微服务架构和部署环境</p>
        </div>
        <div className="row">
          <div className="col col--6">
            <div className={styles.useCaseCard}>
              <h3>🏢 企业级应用</h3>
              <p>为大型企业提供统一的 API 网关，实现服务治理、安全管控和流量管理</p>
              <ul>
                <li>多租户隔离</li>
                <li>企业级安全</li>
                <li>高可用部署</li>
              </ul>
            </div>
          </div>
          <div className="col col--6">
            <div className={styles.useCaseCard}>
              <h3>☁️ 云原生平台</h3>
              <p>与 Kubernetes 深度集成，提供云原生的服务网格和 API 管理能力</p>
              <ul>
                <li>Kubernetes 原生</li>
                <li>服务发现</li>
                <li>配置热更新</li>
              </ul>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
}

function HomepageGettingStarted() {
  return (
    <section className={styles.gettingStarted}>
      <div className="container">
        <div className="text--center">
          <h2>快速开始</h2>
          <p>5 分钟部署您的第一个 TiGateway 实例</p>
          <div className={styles.codeBlock}>
            <pre>
              <code className="language-bash">
{`# 使用 Helm 安装
helm repo add tigateway https://tigateway.github.io/helm-charts
helm install tigateway tigateway/tigateway

# 或使用 kubectl
kubectl apply -f https://raw.githubusercontent.com/tigateway/tigateway/main/deploy/kubernetes.yaml`}
              </code>
            </pre>
          </div>
          <div className={styles.buttons}>
            <Link
              className="button button--primary button--lg"
              to="/docs/quick-start">
              查看完整安装指南
            </Link>
          </div>
        </div>
      </div>
    </section>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title={`${siteConfig.title} - ${siteConfig.tagline}`}
      description="基于 Spring Cloud Gateway 的 Kubernetes 原生 API 网关，提供完整的微服务治理能力">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
        <HomepageCapabilities />
        <HomepageUseCases />
        <HomepageGettingStarted />
      </main>
    </Layout>
  );
}
