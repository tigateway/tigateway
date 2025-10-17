package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:16
 */
@Data
public class LoadBalancer {
    // servers
    private List<Server> servers;
    // pass host header
    private boolean passHostHeader;
}

