package ti.gateway.admin.domain;

import lombok.Data;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:26
 */
@Data
public class ItemStatus {
    // errors
    private int errors;
    // total
    private int total;
    // warnings
    private int warnings;
}

