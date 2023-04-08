package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:17
 */
@Data
public class TcpService {
    private LoadBalancer loadBalancer;
    private String status;
    private List<String> usedBy;

    // Getters and setters
}

