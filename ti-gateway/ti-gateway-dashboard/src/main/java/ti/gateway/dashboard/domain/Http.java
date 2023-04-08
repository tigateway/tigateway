package ti.gateway.dashboard.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:10
 */
@Data
public class Http {
    private ItemStatus middlewares;
    private ItemStatus routers;
    private ItemStatus services;

    // Getters and setters
}

