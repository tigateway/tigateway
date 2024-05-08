package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:39
 */
@Data
public class IPAllowList {
    // IP allow list
    private List<String> sourceRange;
}

