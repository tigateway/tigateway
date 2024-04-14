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
    // insecure
    private boolean insecure;
    // trusted IPs
    private List<String> trustedIPs;
}
