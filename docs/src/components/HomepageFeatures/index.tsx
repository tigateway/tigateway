import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';
import type {FeatureItem} from '../../types';

const FeatureList: FeatureItem[] = [
  {
    title: 'Kubernetes 原生',
    Svg: require('@site/static/img/kubernetes.svg').default,
    description: (
      <>
        TiGateway 专为 Kubernetes 环境设计，提供完整的 CRD 支持和原生集成，
        让您能够像管理其他 Kubernetes 资源一样管理网关配置。
      </>
    ),
  },
  {
    title: 'Spring Cloud Gateway',
    Svg: require('@site/static/img/spring-cloud.svg').default,
    description: (
      <>
        基于 Spring Cloud Gateway 构建，继承了其强大的路由、过滤和负载均衡能力，
        同时针对 Kubernetes 环境进行了深度优化。 
      </>
    ),
  },
  {
    title: '企业级特性',
    Svg: require('@site/static/img/enterprise.svg').default,
    description: (
      <>
        提供限流、熔断、认证授权、监控告警等企业级功能，
        满足生产环境的各种需求。
      </>
    ),
  },
];

function Feature({Svg, title, description}: FeatureItem) {
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
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
