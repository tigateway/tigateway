package ti.gateway.base.storage.db.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 网关应用服务信息
 */
@Data
public class TigaAppServer implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 服务编码
     */
    private String serverCode;

    /**
     * 应用Key
     */
    private String appKey;

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

}
