package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:15
 */
@Data
public class Middleware {
    // Add prefix
    private AddPrefix addPrefix;
    // Basic auth
    private BasicAuth basicAuth;
    // name
    private String name;
    // provider
    private String provider;
    // status
    private String status;
    // type
    private String type;
    // used by
    private List<String> usedBy;
}
