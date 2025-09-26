# API网关基础中间件

基于[Spring Cloud Gateway](https://spring.io/projects/spring-cloud-gateway)扩展实现

## 说明
服务端需要支持Eureka协议注册到Eureka注册中心中，网关会自动服务发现Eureka上注册的服务列表，当发生请求时进行服务端负载均衡。

## 快速部署
1. 初始化SQL(在doc文件夹下)
2. 启动Eureka
3. 修改网关Eureka地址

## 实现功能
* 支持API参数校验 （已完成）
* 支持appId授权服务校验 （已完成）

## 实现原理
 * 在分布式环境下 采用与Eureka Server相同的服务列表缓存更新策略，一级缓存为堆内内存，二级缓存为Guava堆外内存，配置内容保存在持久化设备中（默认数据库）。

架构图
![架构图](./gateway.png)

Spring Cloud 微服务架构网关位置
![SpringCloud 架构](./springcloud.png)


## 模块说明

|模块|说明| 
|:------|:------- |
|gateway-core |网关核心模块 定义网关统一模型和接口规范 |
|gateway-dashboard |网关管理控制台服务 对网关进行运维操作 例如：ak sk下发配置 |
|gateway-limit |网关限流操作模块 对API进行流量降级、熔断等控制 |
|gateway-server |网关服务 对外提供统一API |
|gateway-storage |网关存储模块 定义实现网关相关的存储化功能 默认为数据库 |
|gateway-storage-db |网关存储模块 数据库实现|
|gateway-util |网关工具模块 定义网关需要的特殊工具包|

## 网关配置说明

* 缓存配置说明

```yml
spring:
  cloud:
    gateway:
      cache:
        shouldUseReadOnlyServerCache: true #是否开启服务列表二级缓存配置 默认:开启
        serverCacheAutoExpirationInSeconds: 180 # 二级缓存服务列表缓存自动过期时间 默认: 180秒
        initialCapacityOfServerCache: 100 #初始化二级缓存列表大小 默认: 100
        serverCacheUpdateIntervalMs: 30000 #二级缓存更新一级缓存间隔时间 默认: 30秒
        skip: false #是否跳过缓存 默认:false不跳过
```

* 签名配置说明

```yml
spring:
  cloud:
    gateway:
      app:
        appId: X-GW-APPID #AppID定义的名称 默认:X-GW-APPID
        timestamp: X-GW-Timestamp #时间戳定义的名称 默认:X-GW-Timestamp
        sign: X-GW-SIGN # Sign定义的字段名称 默认:X-GW-SIGN
        durationMt: 30000 #API接口间隔时间 默认: 默认5分钟 1000 * 60 * 5L
        skip: false #是否跳过签名校验 默认:false不跳过
```

* 授权服务配置说明

```yml
spring:
  cloud:
    gateway:
      server:
        skip: false #是否跳过授权服务校验 默认:false不跳过
```

## 版本说明

|版本|修改内容|说明 |
|:------------- |:-----------|:------- |
|1.0.0 | 第一次发布 |支持API参数校验；支持appId授权服务校验|