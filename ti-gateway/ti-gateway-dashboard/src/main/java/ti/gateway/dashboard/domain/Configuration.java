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
    private Map<String, Router> routers;
    private Map<String, Middleware> middlewares;
    private Map<String, Service> services;
    private Map<String, TcpRouter> tcpRouters;
    private Map<String, TcpService> tcpServices;

    // Getters and setters
}

