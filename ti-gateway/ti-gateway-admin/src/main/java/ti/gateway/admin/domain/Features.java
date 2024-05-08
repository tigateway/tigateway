package ti.gateway.admin.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:25
 */
@Data
public class Features {
    // access log
    private boolean accessLog;
    // metrics
    private String metrics;
    // tracing
    private String tracing;
    // hub
    private boolean hub;
}

