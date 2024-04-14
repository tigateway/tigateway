package ti.gateway.dashboard.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:09
 */
@Data
public class AddressConfig {
    // Address configuration
    private String address;
    // forward the client's http headers
    private ForwardedHeaders forwardedHeaders;
    // http configuration
    private Http http;
    // name of the address
    private String name;
    // proxy protocol
    private ProxyProtocol proxyProtocol;
    // transport configuration
    private Transport transport;
}

