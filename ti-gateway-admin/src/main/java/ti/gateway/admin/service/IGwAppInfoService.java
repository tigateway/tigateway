package ti.gateway.admin.service;

import ti.gateway.admin.service.entity.GwAppInfoEntity;
import ti.gateway.admin.base.Page;
import ti.gateway.admin.service.dto.GwAppInfoDto;

/**
 * 应用信息接口
 */
public interface IGwAppInfoService {

    /**
     * 查询应用列表信息
     *
     * @param gwAppInfoDto 应用信息
     * @param currentPage 当前页
     * @param pageSize 每页显示数量
     * @return 应用列表信息
     */
    Page<GwAppInfoEntity> queryGwAppInfos(GwAppInfoDto gwAppInfoDto, int currentPage, int pageSize);

}
