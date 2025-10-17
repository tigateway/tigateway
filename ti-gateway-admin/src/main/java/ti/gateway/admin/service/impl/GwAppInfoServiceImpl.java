package ti.gateway.admin.service.impl;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import ti.gateway.admin.service.entity.GwAppInfoEntity;
import ti.gateway.admin.base.Page;
import ti.gateway.admin.service.IGwAppInfoService;
import ti.gateway.admin.service.dto.GwAppInfoDto;

/**
 * 应用信息服务实现类
 * 暂时返回空数据，等待ConfigMap集成完成
 */
@Service
@Primary
public class GwAppInfoServiceImpl implements IGwAppInfoService {

    @Override
    public Page<GwAppInfoEntity> queryGwAppInfos(GwAppInfoDto gwAppInfoDto, int currentPage, int pageSize) {
        try {
            // 暂时返回空分页，等待ConfigMap集成完成
            return new Page<>();
        } catch (Exception e) {
            // 如果获取失败，返回空分页
            return new Page<>();
        }
    }
}