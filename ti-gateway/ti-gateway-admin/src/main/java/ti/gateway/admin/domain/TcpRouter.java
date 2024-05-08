package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:17
 */
@Data
public class TcpRouter {
    // entryPoints
    private List<String> entryPoints;
    // service
    private String service;
    // rule
    private String rule;
    // status
    private String status;
}

