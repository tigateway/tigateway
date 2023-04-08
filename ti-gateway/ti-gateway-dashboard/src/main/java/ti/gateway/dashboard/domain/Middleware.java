package ti.gateway.dashboard.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:15
 */
@Data
public class Middleware {
    private AddPrefix addPrefix;
    private BasicAuth basicAuth;
    private String name;
    private String provider;
    private String status;
    private String type;
    private List<String> usedBy;

    // Getters and setters
}
