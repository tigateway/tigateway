package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:44
 */
@Data
public class LoadBalancerInfo {
    // Load balancer
    private LoadBalancer loadBalancer;
    // name
    private String name;
    // provider
    private String provider;
    // status
    private String status;
    // type
    private String type;
    // used by
    private List<String> usedBy;
}

