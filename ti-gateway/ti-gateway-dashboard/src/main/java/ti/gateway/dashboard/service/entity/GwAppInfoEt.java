package ti.gateway.dashboard.service.entity;

import java.util.Date;

public class GwAppInfoEt {

    private Long id;

    /**
     * 应用名称
     */
    private String name;

    /**
     * 应用描述
     */
    private String desc;

    /**
     * 应用类型 1 PC 2APP
     */
    private Byte type;

    /**
     * 应用Key
     */
    private String appkey;

    /**
     * 应用Secret
     */
    private String appsecret;

    /**
     * 应用状态 0不可用 1可用
     */
    private Byte status;
    /**
     * 创建时间
     */
    private Date ctime;
    /**
     * 修改时间
     */
    private Date mtime;

    private static final long serialVersionUID = 1L;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
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

    public String getAppsecret() {
        return appsecret;
    }

    public void setAppsecret(String appsecret) {
        this.appsecret = appsecret;
    }

    public Byte getStatus() {
        return status;
    }

    public void setStatus(Byte status) {
        this.status = status;
    }

    public Date getCtime() {
        return ctime;
    }

    public void setCtime(Date ctime) {
        this.ctime = ctime;
    }

    public Date getMtime() {
        return mtime;
    }

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

}
