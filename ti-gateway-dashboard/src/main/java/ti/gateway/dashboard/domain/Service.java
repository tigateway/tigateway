package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:16
 */
@Data
public class Service {
    // load balancer
    private LoadBalancer loadBalancer;
    // name
    private String status;
    // used by
    private List<String> usedBy;
}
