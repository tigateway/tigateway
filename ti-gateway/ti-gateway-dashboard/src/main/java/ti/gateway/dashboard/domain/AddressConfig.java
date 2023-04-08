package ti.gateway.dashboard.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:09
 */
@Data
public class AddressConfig {
    private String address;
    private ForwardedHeaders forwardedHeaders;
    private Http http;
    private String name;
    private ProxyProtocol proxyProtocol;
    private Transport transport;

    // Getters and setters
}

