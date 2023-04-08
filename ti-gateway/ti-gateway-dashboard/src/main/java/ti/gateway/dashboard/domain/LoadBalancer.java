package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:16
 */
@Data
public class LoadBalancer {
    private List<Server> servers;
    private boolean passHostHeader;

    // Getters and setters
}

