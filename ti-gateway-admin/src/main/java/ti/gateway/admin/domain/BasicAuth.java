package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:16
 */
@Data
public class BasicAuth {
    // Basic authentication
    private List<String> users;
}

