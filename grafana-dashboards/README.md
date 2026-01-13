# TiGateway Grafana Dashboards

本目录包含 TiGateway 的 Grafana 监控仪表板，用于监控和可视化 TiGateway 的运行状态和性能指标。

## 📊 Dashboard 列表

### 1. TiGateway Overview (`tigateway-overview.json`)

总体概览仪表板，提供 TiGateway 的关键指标概览：

- **关键指标卡片**：
  - 请求率 (Request Rate)
  - 错误率 (Error Rate)
  - P95 延迟 (P95 Latency)
  - 活跃路由数 (Active Routes)

- **趋势图表**：
  - 按状态码分类的请求率趋势
  - 请求延迟分布 (P50, P95, P99)
  - 错误率趋势
  - 按路由的请求率统计表
  - CPU 使用率
  - 内存使用情况

**适用场景**：日常监控、快速查看系统健康状态

### 2. TiGateway Routes (`tigateway-routes.json`)

路由详细监控仪表板，专注于路由级别的性能分析：

- **路由性能汇总表**：包含请求率、P95 延迟、错误率
- **按路由的请求率趋势**
- **按路由的 P95 延迟趋势**
- **按路由的错误率趋势**
- **最近 1 小时的总请求数统计**

**适用场景**：路由性能分析、问题排查、路由优化

### 3. TiGateway Performance (`tigateway-performance.json`)

性能指标仪表板，深入分析系统性能：

- **延迟百分位数**：P50, P90, P95, P99
- **按 HTTP 方法的请求率**
- **请求延迟热力图**：可视化延迟分布
- **按状态码分类的请求率**：2xx, 3xx, 4xx, 5xx
- **成功率/错误率趋势**

**适用场景**：性能优化、容量规划、SLA 监控

### 4. TiGateway JVM (`tigateway-jvm.json`)

JVM 监控仪表板，监控 Java 虚拟机运行状态：

- **关键指标卡片**：
  - 堆内存使用率
  - 活跃线程数
  - GC 暂停时间
  - GC 频率

- **趋势图表**：
  - 按区域的内存使用情况 (Heap, Non-Heap)
  - 线程统计 (Live, Peak, Daemon)
  - 按操作的 GC 暂停时间
  - 按操作的 GC 频率

**适用场景**：JVM 调优、内存泄漏排查、GC 分析

## 🚀 快速开始

### 前置条件

1. **已部署 TiGateway**：确保 TiGateway 已部署并运行
2. **已配置 Prometheus**：确保 Prometheus 正在收集 TiGateway 的指标
3. **已安装 Grafana**：确保 Grafana 已安装并可访问

### 导入 Dashboard

#### 方法一：通过 Grafana UI 导入

1. 登录 Grafana
2. 点击左侧菜单的 **"+"** → **"Import"**
3. 点击 **"Upload JSON file"** 或直接粘贴 JSON 内容
4. 选择对应的 JSON 文件（例如 `tigateway-overview.json`）
5. 选择 Prometheus 数据源
6. 点击 **"Import"**

#### 方法二：通过 Grafana API 导入

```bash
# 导入 Overview Dashboard
curl -X POST \
  http://admin:admin@localhost:3000/api/dashboards/db \
  -H 'Content-Type: application/json' \
  -d @tigateway-overview.json

# 导入 Routes Dashboard
curl -X POST \
  http://admin:admin@localhost:3000/api/dashboards/db \
  -H 'Content-Type: application/json' \
  -d @tigateway-routes.json

# 导入 Performance Dashboard
curl -X POST \
  http://admin:admin@localhost:3000/api/dashboards/db \
  -H 'Content-Type: application/json' \
  -d @tigateway-performance.json

# 导入 JVM Dashboard
curl -X POST \
  http://admin:admin@localhost:3000/api/dashboards/db \
  -H 'Content-Type: application/json' \
  -d @tigateway-jvm.json
```

### 配置 Prometheus 数据源

确保 Grafana 中已配置 Prometheus 数据源：

1. 进入 **Configuration** → **Data Sources**
2. 点击 **"Add data source"**
3. 选择 **"Prometheus"**
4. 配置 Prometheus 服务器 URL（例如：`http://prometheus:9090`）
5. 点击 **"Save & Test"**

## 📈 指标说明

### Spring Cloud Gateway 指标

TiGateway 基于 Spring Cloud Gateway，使用标准的 Spring Cloud Gateway 指标：

- `spring_cloud_gateway_requests_seconds_count`：请求总数（Counter）
- `spring_cloud_gateway_requests_seconds_bucket`：请求延迟直方图（Histogram）
- `spring_cloud_gateway_requests_seconds_sum`：请求总延迟（Summary）
- `spring_cloud_gateway_routes_count`：路由数量（Gauge）

### JVM 指标

标准的 Micrometer JVM 指标：

- `jvm_memory_used_bytes`：JVM 内存使用量
- `jvm_memory_max_bytes`：JVM 最大内存
- `jvm_threads_live_threads`：活跃线程数
- `jvm_gc_pause_seconds`：GC 暂停时间

### 系统指标

- `process_cpu_seconds_total`：进程 CPU 使用时间
- `system_cpu_usage`：系统 CPU 使用率

## 🔧 配置 TiGateway 指标导出

确保 TiGateway 已配置 Prometheus 指标导出：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
```

## 📝 变量说明

所有 Dashboard 都支持以下变量：

- **`$namespace`**：Kubernetes 命名空间（可选，默认显示所有命名空间）
- **`$route`**（仅 Routes Dashboard）：路由 ID（可选，默认显示所有路由）

## 🎨 自定义 Dashboard

### 修改刷新间隔

在 Dashboard JSON 中修改 `refresh` 字段：

```json
{
  "refresh": "30s"  // 修改为所需的刷新间隔
}
```

### 修改时间范围

在 Dashboard JSON 中修改 `time` 字段：

```json
{
  "time": {
    "from": "now-15m",  // 修改起始时间
    "to": "now"         // 修改结束时间
  }
}
```

### 添加新的面板

1. 在 Grafana UI 中编辑 Dashboard
2. 添加新的面板
3. 配置 Prometheus 查询表达式
4. 导出 JSON 并更新对应的文件

## 🔍 故障排查

### Dashboard 显示 "No Data"

1. **检查 Prometheus 数据源**：
   - 确认 Prometheus 数据源配置正确
   - 测试数据源连接

2. **检查指标是否存在**：
   ```bash
   # 在 Prometheus 中查询指标
   curl http://prometheus:9090/api/v1/query?query=spring_cloud_gateway_requests_seconds_count
   ```

3. **检查标签匹配**：
   - 确认指标标签与 Dashboard 查询中的标签匹配
   - 检查 `namespace` 变量是否正确

### 指标名称不匹配

如果您的 TiGateway 使用不同的指标名称，需要修改 Dashboard 中的查询表达式：

1. 在 Grafana 中编辑 Dashboard
2. 修改面板的查询表达式
3. 更新指标名称以匹配您的配置

## 📚 相关资源

- [TiGateway 监控文档](../../docs/docs/monitoring-and-metrics.md)
- [Prometheus 文档](https://prometheus.io/docs/)
- [Grafana 文档](https://grafana.com/docs/)
- [Spring Cloud Gateway 指标](https://docs.spring.io/spring-cloud-gateway/docs/current/reference/html/#actuator-api)

## 🤝 贡献

欢迎提交 Issue 和 Pull Request 来改进这些 Dashboard！

## 📄 许可证

本项目采用 Apache 2.0 许可证。
