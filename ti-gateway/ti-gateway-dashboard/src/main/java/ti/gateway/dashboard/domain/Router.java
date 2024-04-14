package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:15
 */
@Data
public class Router {
    // entryPoints
    private List<String> entryPoints;
    // middlewares
    private List<String> middlewares;
    // name
    private String name;
    // provider
    private String provider;
    // service
    private String service;
    // rule
    private String rule;
    // status
    private String status;
    // using
    private List<String> using;
}

