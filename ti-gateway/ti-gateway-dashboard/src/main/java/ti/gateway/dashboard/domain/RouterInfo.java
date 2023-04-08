package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:32
 */
@Data
public class RouterInfo {
    private List<String> entryPoints;
    private List<String> middlewares;
    private String name;
    private String provider;
    private String rule;
    private String service;
    private String status;
    private TLS tls;
    private List<String> using;

    // Getters and setters
}