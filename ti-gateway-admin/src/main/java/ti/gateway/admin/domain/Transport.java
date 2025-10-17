package ti.gateway.admin.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:11
 */
@Data
public class Transport {
    // lifeCycle
    private LifeCycle lifeCycle;
    // responding Timeouts
    private RespondingTimeouts respondingTimeouts;
}

