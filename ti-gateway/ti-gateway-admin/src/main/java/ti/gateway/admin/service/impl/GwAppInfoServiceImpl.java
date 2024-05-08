package ti.gateway.admin.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ti.gateway.admin.service.entity.GwAppInfoEntity;
import ti.gateway.admin.base.Page;
import ti.gateway.admin.service.IGwAppInfoService;
import ti.gateway.admin.service.dto.GwAppInfoDto;
import ti.gateway.base.storage.db.mapper.TigaAppInfoMapper;
import ti.gateway.base.storage.db.model.TigaAppInfo;
import ti.gateway.base.util.BeanCopierUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GwAppInfoServiceImpl implements IGwAppInfoService {

    @Resource
    private TigaAppInfoMapper gwAppInfoMapper;

    @Override
    public Page<GwAppInfoEntity> queryGwAppInfos(GwAppInfoDto gwAppInfnDto, int currentPage, int pageSize) {
        Page<GwAppInfoEntity> pager = null;
        TigaAppInfo gwAppInfo = BeanCopierUtils.copierTargetBean(gwAppInfnDto, GwAppInfoDto.class, TigaAppInfo.class);
        int sum = gwAppInfoMapper.selectCountWithBySearch(gwAppInfo);
        if (sum > 0) {
            pager = new Page<>(sum, currentPage, pageSize);
            List<TigaAppInfo> gwAppInfos = gwAppInfoMapper.selectListWithBySearch(gwAppInfo, pager.getStart(), pager.getPageSize());
            List<GwAppInfoEntity> gwAppInfoEts = BeanCopierUtils.copierTargetBeanList(gwAppInfos, TigaAppInfo.class, GwAppInfoEntity.class);
            pager.setList(gwAppInfoEts);
        } else {
            pager = new Page<>();
        }
        return pager;
    }

}
