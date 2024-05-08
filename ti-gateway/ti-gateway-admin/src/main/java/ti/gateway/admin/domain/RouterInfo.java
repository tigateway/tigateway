package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:32
 */
@Data
public class RouterInfo {
    // Entry points
    private List<String> entryPoints;
    // Middlewares
    private List<String> middlewares;
    // name
    private String name;
    // provider
    private String provider;
    // rule
    private String rule;
    // service
    private String service;
    // status
    private String status;
    // TLS
    private TLS tls;
    // using
    private List<String> using;
}