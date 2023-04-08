package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:17
 */
@Data
public class TcpRouter {
    private List<String> entryPoints;
    private String service;
    private String rule;
    private String status;

    // Getters and setters
}

