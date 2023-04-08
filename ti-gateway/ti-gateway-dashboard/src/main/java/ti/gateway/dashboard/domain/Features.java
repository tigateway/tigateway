package ti.gateway.dashboard.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:25
 */
@Data
public class Features {
    private boolean accessLog;
    private String metrics;
    private String tracing;
    private boolean hub;

    // Getters and setters
}

