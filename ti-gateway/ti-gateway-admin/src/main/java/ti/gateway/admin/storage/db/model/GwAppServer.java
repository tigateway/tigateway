package ti.gateway.admin.storage.db.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 网关应用服务信息
 */
public class GwAppServer implements Serializable {

    private Long id;

    /**
     * 服务编码
     */
    private String serverCode;

    /**
     * 应用Key
     */
    private String appkey;

    /**
     * 服务授权IP地址
     */
    private String serverIps;

    /**
     * 服务状态 0不可用 1可用
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

    public String getServerCode() {
        return serverCode;
    }

    public void setServerCode(String serverCode) {
        this.serverCode = serverCode;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getServerIps() {
        return serverIps;
    }

    public void setServerIps(String serverIps) {
        this.serverIps = serverIps;
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
