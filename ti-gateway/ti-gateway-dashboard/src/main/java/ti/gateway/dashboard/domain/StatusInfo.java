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
    private Features features;
    private Http http;
    private Tcp tcp;
    private Udp udp;
    private List<String> providers;

    // Getters and setters
}


