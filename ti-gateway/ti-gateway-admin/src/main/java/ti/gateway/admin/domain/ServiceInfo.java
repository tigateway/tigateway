package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:34
 */
@Data
public class ServiceInfo {
    // load balancer
    private LoadBalancer loadBalancer;
    // name
    private String name;
    // provider
    private String provider;
    // server status
    private ServerStatus serverStatus;
    // status
    private String status;
    // type
    private String type;
    // used by
    private List<String> usedBy;
    // weight sticky
    private WeightedSticky weighted;
    // mirroring
    private Mirroring mirroring;
}

