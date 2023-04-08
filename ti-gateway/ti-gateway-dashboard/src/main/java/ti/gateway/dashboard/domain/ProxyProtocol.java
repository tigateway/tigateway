package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:10
 */
@Data
public class ProxyProtocol {
    private boolean insecure;
    private List<String> trustedIPs;

    // Getters and setters
}
