package ti.gateway.admin.service;

import ti.gateway.admin.service.entity.GwAppInfoEt;
import ti.gateway.admin.base.Page;
import ti.gateway.admin.service.dto.GwAppInfnDto;

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
