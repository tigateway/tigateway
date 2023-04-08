package ti.gateway.dashboard.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:26
 */
@Data
public class Tcp {
    private ItemStatus middlewares;
    private ItemStatus routers;
    private ItemStatus services;

    // Getters and setters
}

