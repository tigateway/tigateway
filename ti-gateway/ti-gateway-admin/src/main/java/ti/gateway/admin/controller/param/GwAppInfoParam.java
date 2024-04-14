package ti.gateway.admin.controller.param;

import javax.validation.constraints.NotNull;

/**
 * 网关应用配置Vo
 */
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

    public Integer getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(Integer currentPage) {
        this.currentPage = currentPage;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Byte getType() {
        return type;
    }

    public void setType(Byte type) {
        this.type = type;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

}
