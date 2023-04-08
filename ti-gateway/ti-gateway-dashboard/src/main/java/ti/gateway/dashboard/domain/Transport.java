package ti.gateway.dashboard.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:11
 */
@Data
public class Transport {
    private LifeCycle lifeCycle;
    private RespondingTimeouts respondingTimeouts;

    // Getters and setters
}

