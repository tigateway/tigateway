package ti.gateway.dashboard.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:10
 */
@Data
public class Http {
    // Http middleware
    private ItemStatus middlewares;
    // Http routes
    private ItemStatus routers;
    // Http services
    private ItemStatus services;
}

