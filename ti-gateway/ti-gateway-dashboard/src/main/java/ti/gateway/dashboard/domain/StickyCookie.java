package ti.gateway.dashboard.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:35
 */
@Data
public class StickyCookie {
    private boolean httpOnly;
    private String name;
    private boolean secure;

    // Getters and setters
}

