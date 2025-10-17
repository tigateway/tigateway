package ti.gateway.admin.domain;

import lombok.Data;

import java.util.List;

/**
 * @author wangzhengdong
 * @version 1.0
 * @date 2023/4/9 01:35
 */
@Data
public class Mirroring {
    // Mirrors
    private List<Mirror> mirrors;
    // Service
    private String service;
}

