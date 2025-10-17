package ti.gateway.admin.controller.param;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 网关应用配置Vo
 */
@Data
public class GwAppInfoParam {

    @NotNull(message = "当前页不得为空")
    private Integer currentPage;
    @NotNull(message = "页面个数不得为空")
    private Integer pageSize;
    /**
     * 应用名称
     */
    private String name;
    /**
     * 应用类型 1 PC 2APP
     */
    private Byte type;
    /**
     * 应用Key
     */
    private String appkey;
    /**
     * 应用状态 0不可用 1可用
     */
    private Byte status;

}
