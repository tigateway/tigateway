package ti.gateway.admin.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:35
 */
@Data
public class StickyCookie {
    // http only
    private boolean httpOnly;
    // name
    private String name;
    // secure
    private boolean secure;
}

