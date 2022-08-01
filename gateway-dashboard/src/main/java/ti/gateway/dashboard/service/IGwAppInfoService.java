package ti.gateway.dashboard.service;

import ti.gateway.dashboard.base.Page;
import ti.gateway.dashboard.service.dto.GwAppInfnDto;
import ti.gateway.dashboard.service.entity.GwAppInfoEt;

/**
 * 应用信息接口
 */
public interface IGwAppInfoService {

    /**
     * 查询应用列表信息
     *
     * @param gwAppInfnDto
     * @param currentPage
     * @param pageSize
     * @return
     */
    Page<GwAppInfoEt> queryGwAppInfos(GwAppInfnDto gwAppInfnDto, int currentPage, int pageSize);

}
