package ti.gateway.admin.service.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class GwAppInfoDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 应用名称
     */
    private String name;
    /**
     * 应用类型 1 PC, 2 APP
     */
    private Byte type;
    /**
     * 应用Key
     */
    private String appKey;
    /**
     * 应用状态 0不可用 1可用
     */
    private Byte status;

}
