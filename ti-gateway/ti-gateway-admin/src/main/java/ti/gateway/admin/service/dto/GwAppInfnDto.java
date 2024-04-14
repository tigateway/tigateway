package ti.gateway.admin.service.dto;

public class GwAppInfnDto {

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
