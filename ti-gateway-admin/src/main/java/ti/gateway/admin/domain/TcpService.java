package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:17
 */
@Data
public class TcpService {
    // Load balancer
    private LoadBalancer loadBalancer;
    // status
    private String status;
    // used by
    private List<String> usedBy;
}

