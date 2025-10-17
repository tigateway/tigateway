package ti.gateway.admin.service.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class GwAppInfoEntity implements Serializable {

    private static final long serialVersionUID = 1L;

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
    private String appKey;

    /**
     * 应用Secret
     */
    private String appSecret;

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




}
