package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.Map;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:36
 */
@Data
public class ServerStatus {
    // Server status
    private Map<String, String> serverStatus;
}

