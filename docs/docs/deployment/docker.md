# Docker 部署

本文档介绍如何使用 Docker 部署 TiGateway。

## 前提条件

- Docker 20.10+
- Docker Compose 2.0+

## 快速开始

### 1. 拉取镜像

```bash
docker pull tigateway/tigateway:latest
```

### 2. 运行容器

```bash
docker run -d \
  --name tigateway \
  -p 8080:8080 \
  -p 9090:9090 \
  -e SPRING_PROFILES_ACTIVE=docker \
  tigateway/tigateway:latest
```

### 3. 验证部署

```bash
curl http://localhost:8080/actuator/health
```

## Docker Compose 部署

创建 `docker-compose.yml` 文件：

```yaml
version: '3.8'

services:
  tigateway:
    image: tigateway/tigateway:latest
    container_name: tigateway
    ports:
      - "8080:8080"
      - "9090:9090"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - SPRING_CLOUD_GATEWAY_ROUTES[0].ID=example-route
      - SPRING_CLOUD_GATEWAY_ROUTES[0].URI=http://httpbin.org
      - SPRING_CLOUD_GATEWAY_ROUTES[0].PREDICATES[0]=Path=/api/**
    volumes:
      - ./config:/app/config
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
```

启动服务：

```bash
docker-compose up -d
```

## 配置管理

### 环境变量

| 变量名 | 描述 | 默认值 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | 激活的配置文件 | `default` |
| `SERVER_PORT` | 服务端口 | `8080` |
| `MANAGEMENT_PORT` | 管理端口 | `9090` |

### 配置文件挂载

将配置文件挂载到容器中：

```bash
docker run -d \
  --name tigateway \
  -p 8080:8080 \
  -v $(pwd)/config:/app/config \
  tigateway/tigateway:latest
```

## 监控和日志

### 健康检查

```bash
docker exec tigateway curl http://localhost:8080/actuator/health
```

### 查看日志

```bash
docker logs -f tigateway
```

### 指标监控

访问 `http://localhost:9090/actuator/metrics` 查看运行指标。

## 故障排除

### 常见问题

1. **端口冲突**
   ```bash
   # 检查端口占用
   netstat -tulpn | grep :8080
   ```

2. **容器无法启动**
   ```bash
   # 查看容器日志
   docker logs tigateway
   ```

3. **配置错误**
   ```bash
   # 进入容器检查配置
   docker exec -it tigateway sh
   ```

### 性能优化

1. **资源限制**
   ```yaml
   services:
     tigateway:
       deploy:
         resources:
           limits:
             memory: 1G
             cpus: '0.5'
   ```

2. **JVM 调优**
   ```bash
   docker run -d \
     --name tigateway \
     -e JAVA_OPTS="-Xms512m -Xmx1g" \
     tigateway/tigateway:latest
   ```

## 生产环境建议

1. 使用具体的镜像标签而不是 `latest`
2. 配置资源限制
3. 启用健康检查
4. 配置日志轮转
5. 使用外部配置中心
6. 启用 TLS/SSL
