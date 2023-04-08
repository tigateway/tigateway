package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:44
 */
@Data
public class LoadBalancerInfo {
    private LoadBalancer loadBalancer;
    private String name;
    private String provider;
    private String status;
    private String type;
    private List<String> usedBy;

    // Getters and setters
}

