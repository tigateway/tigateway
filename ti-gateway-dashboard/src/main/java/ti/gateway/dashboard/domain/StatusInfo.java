package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:25
 */
@Data
public class StatusInfo {
    // features
    private Features features;
    // http
    private Http http;
    // tcp
    private Tcp tcp;
    // udp
    private Udp udp;
    // providers
    private List<String> providers;
}


