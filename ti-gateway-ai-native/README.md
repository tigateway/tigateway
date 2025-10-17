# TiGateway AI Native

TiGateway AI Native 是一个专为AI应用设计的原生API网关，提供完整的AI开发、安全防护、多模型适配和可观测性功能。

## 🚀 核心特性

### 1. AI开发插件集
- **LLM缓存**: 提供LLM响应的缓存功能，提高性能并降低成本
- **提示词模板**: 管理和应用提示词模板，标准化AI交互
- **提示词装饰器**: 动态装饰和增强提示词内容
- **请求转换**: 适配不同AI服务的请求格式
- **响应转换**: 标准化AI服务的响应格式
- **向量检索**: 集成向量数据库，支持RAG应用

### 2. AI安全防护
- **内容审核**: 集成阿里云内容安全，提供多维度内容审核
- **Token限流**: 基于Token使用量的智能限流
- **Token配额**: 用户级别的Token使用配额管理

### 3. 多模型适配
- **AI代理**: 统一的AI服务代理，支持协议转换
- **重试机制**: 自动重试失败的请求
- **Fallback**: 智能降级和备用模型切换
- **模型管理**: 支持多种AI模型提供商

### 4. 可观测性
- **AI统计**: 实时AI请求统计和监控
- **LLM访问日志**: 详细的LLM交互日志
- **Token消费观测**: Token使用量实时监控
- **可用性告警**: 服务可用性监控和告警

## 🏗️ 架构设计

```
┌─────────────────────────────────────────────────────────────┐
│                    TiGateway AI Native                      │
├─────────────────────────────────────────────────────────────┤
│  AI开发插件集          │  AI安全防护          │  多模型适配    │
│  • LLM缓存            │  • 内容审核          │  • AI代理      │
│  • 提示词模板          │  • Token限流         │  • 重试机制    │
│  • 提示词装饰器        │  • Token配额         │  • Fallback   │
│  • 请求转换            │                     │  • 模型管理    │
│  • 响应转换            │                     │               │
│  • 向量检索            │                     │               │
├─────────────────────────────────────────────────────────────┤
│                        可观测性                             │
│  • AI统计  • LLM访问日志  • Token消费观测  • 可用性告警      │
├─────────────────────────────────────────────────────────────┤
│                    外部集成                                │
│  LLMs: OpenAI, Anthropic, 阿里云, 智谱AI, 月之暗面...      │
│  向量数据库: Redis, 阿里云DashVector, 阿里云Lindorm...     │
│  内容审核: 阿里云内容安全, 第三方SaaS服务...               │
└─────────────────────────────────────────────────────────────┘
```

## 📦 支持的AI模型

### OpenAI
- GPT-3.5-turbo
- GPT-4
- GPT-4-turbo

### Anthropic
- Claude-3-sonnet
- Claude-3-haiku
- Claude-3-opus

### 阿里云
- 通义千问 (qwen-turbo, qwen-plus, qwen-max)
- 通义万相

### 智谱AI
- GLM-4
- GLM-3-turbo

### 月之暗面
- moonshot-v1-8k
- moonshot-v1-32k

### 其他
- DeepSeek
- 阶跃星辰
- 文心一言
- Google Gemini

## 🛠️ 快速开始

### 1. 环境要求
- Java 17+
- Spring Boot 3.2+
- Redis 6.0+
- Maven 3.6+

### 2. 配置API密钥
```bash
export OPENAI_API_KEY=your_openai_api_key
export ANTHROPIC_API_KEY=your_anthropic_api_key
export ALIBABA_API_KEY=your_alibaba_api_key
```

### 3. 启动应用
```bash
mvn spring-boot:run
```

### 4. 测试AI请求
```bash
curl -X POST http://localhost:8083/ai-native/ai/chat/completions \
  -H "Content-Type: application/json" \
  -H "X-Target-Model: gpt-3.5-turbo" \
  -H "X-Prompt-Template: default" \
  -d '{
    "messages": [
      {"role": "user", "content": "Hello, AI!"}
    ],
    "max_tokens": 100
  }'
```

## ⚙️ 配置说明

### 基础配置
```yaml
tigateway:
  ai:
    enabled: true
    ai-development:
      llm-cache:
        enabled: true
        max-size: 1000
        ttl: 3600
    ai-security:
      content-review:
        enabled: true
        provider: alibaba
      token-rate-limit:
        enabled: true
        requests-per-minute: 100
        tokens-per-minute: 10000
```

### 模型配置
```yaml
tigateway:
  ai:
    multi-model:
      models:
        - name: gpt-3.5-turbo
          provider: openai
          endpoint: https://api.openai.com/v1
          api-key: ${OPENAI_API_KEY:}
          parameters:
            max-tokens: 4096
            temperature: 0.7
```

## 🔌 插件开发

### 自定义AI插件
```java
@Component
public class CustomAiPlugin extends AbstractGatewayFilterFactory<CustomAiPlugin.Config> {
    
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            // 自定义AI处理逻辑
            return chain.filter(exchange);
        };
    }
    
    public static class Config {
        // 插件配置
    }
}
```

### 自定义模型提供者
```java
@Component
public class CustomModelProvider implements AiProxyService.ModelProvider {
    
    @Override
    public Mono<LlmResponse> proxyRequest(LlmRequest request) {
        // 自定义模型请求逻辑
        return Mono.just(new LlmResponse());
    }
}
```

## 📊 监控和指标

### 访问统计端点
```bash
# 获取AI统计信息
curl http://localhost:8083/ai-native/actuator/ai-stats

# 获取Prometheus指标
curl http://localhost:8083/ai-native/actuator/prometheus
```

### 关键指标
- `ai_requests_total`: AI请求总数
- `ai_requests_duration_seconds`: AI请求持续时间
- `ai_token_usage_total`: Token使用总量
- `ai_cache_hits_total`: 缓存命中次数
- `ai_rate_limit_exceeded_total`: 限流触发次数

## 🔒 安全特性

### 内容审核
- 文本内容审核
- 图像内容审核
- 多维度风险评估
- 实时拦截机制

### 限流和配额
- 基于用户的请求限流
- 基于Token使用量的限流
- 日/月Token配额管理
- 智能降级策略

## 🚀 部署

### Docker部署
```dockerfile
FROM openjdk:17-jre-slim
COPY target/ti-gateway-ai-native-1.0.0.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Kubernetes部署
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: tigateway-ai-native
spec:
  replicas: 3
  selector:
    matchLabels:
      app: tigateway-ai-native
  template:
    metadata:
      labels:
        app: tigateway-ai-native
    spec:
      containers:
      - name: tigateway-ai-native
        image: tigateway/ai-native:1.0.0
        ports:
        - containerPort: 8083
        env:
        - name: OPENAI_API_KEY
          valueFrom:
            secretKeyRef:
              name: ai-secrets
              key: openai-api-key
```

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 Apache 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🙏 致谢

- 感谢 [Higress](https://github.com/alibaba/higress) 项目的启发
- 感谢所有开源AI模型提供商
- 感谢社区贡献者的支持

## 📞 联系我们

- 项目主页: https://github.com/your-org/tigateway
- 问题反馈: https://github.com/your-org/tigateway/issues
- 邮箱: tigateway@example.com
