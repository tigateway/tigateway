# TiGateway Admin Integration

## 概述

TiGateway Admin功能已成功集成到主Gateway应用中，实现了统一的服务架构。

## 架构变更

### 之前架构
```
TiGateway Gateway (端口8080) + TiGateway Admin (端口8081) = 两个独立服务
```

### 现在架构
```
TiGateway Gateway (端口8080) + Admin功能 (路径/admin) = 统一服务
```

## 功能访问

### Gateway功能
- **主服务端口**: 8080
- **管理端口**: 8090 (Actuator)
- **访问路径**: 直接访问根路径

### Admin功能
- **访问路径**: `/admin/*`
- **API端点**: `/admin/api/*`
- **管理界面**: `/admin/app/*`

## 主要端点

### Admin API端点
- `GET /admin/api/overview` - 系统概览
- `GET /admin/api/version` - 版本信息
- `GET /admin/api/http/routers` - HTTP路由列表
- `GET /admin/api/http/services` - HTTP服务列表
- `GET /admin/api/http/middlewares` - HTTP中间件列表
- `GET /admin/api/gateway/routes` - Gateway路由管理
- `POST /admin/api/gateway/refresh` - 刷新路由

### 应用管理
- `GET /admin/app/list` - 应用列表页面
- `GET /admin/app/queryGwAppInfos` - 查询应用信息

## 配置说明

### application.yml配置
```yaml
# Admin配置
admin:
  server:
    port: 8081  # 预留配置，当前使用路径区分
  context-path: /admin

# Gateway配置
server:
  port: 8080

management:
  server:
    port: 8090
```

### 依赖关系
- Gateway应用依赖Admin模块
- Admin模块依赖Base模块
- 使用ConfigMap存储替代MySQL数据库

## 部署说明

### 单服务部署
```bash
# 启动Gateway应用（包含Admin功能）
java -jar ti-gateway-kubernetes-1.0.0.jar
```

### 访问方式
- Gateway服务: http://localhost:8080
- Admin管理: http://localhost:8080/admin
- 管理端点: http://localhost:8090/actuator

## 优势

1. **简化部署**: 只需部署一个服务
2. **统一管理**: 所有功能在一个进程中
3. **资源共享**: 共享配置和依赖
4. **降低复杂度**: 减少服务间通信
5. **云原生**: 完全基于ConfigMap存储

## 迁移指南

### 从独立Admin服务迁移
1. 停止独立的Admin服务
2. 启动集成的Gateway服务
3. 更新访问URL（添加/admin前缀）
4. 验证所有功能正常

### URL变更
- 旧: `http://admin-service:8081/api/overview`
- 新: `http://gateway-service:8080/admin/api/overview`
