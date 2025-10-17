package ti.gateway.admin.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:26
 */
@Data
public class Udp {
    // routers
    private ItemStatus routers;
    // services
    private ItemStatus services;
}

