package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.Map;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:14
 */
@Data
public class Configuration {
    // router configuration
    private Map<String, Router> routers;
    // middleware configuration
    private Map<String, Middleware> middlewares;
    // service configuration
    private Map<String, Service> services;
    // tcp router configuration
    private Map<String, TcpRouter> tcpRouters;
    // tcp service configuration
    private Map<String, TcpService> tcpServices;
}

