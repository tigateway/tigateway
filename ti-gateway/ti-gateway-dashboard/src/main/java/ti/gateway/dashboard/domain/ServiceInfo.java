package ti.gateway.dashboard.domain;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:34
 */
import java.util.List;

public class ServiceInfo {
    private LoadBalancer loadBalancer;
    private String name;
    private String provider;
    private ServerStatus serverStatus;
    private String status;
    private String type;
    private List<String> usedBy;
    private WeightedSticky weighted;
    private Mirroring mirroring;

    // Getters and setters
}

