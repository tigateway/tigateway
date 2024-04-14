package ti.gateway.dashboard.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:10
 */
@Data
public class RespondingTimeouts {
    // idle timeout
    private String idleTimeout;
    // read timeout
    private String readTimeout;
    // write timeout
    private String writeTimeout;
}

