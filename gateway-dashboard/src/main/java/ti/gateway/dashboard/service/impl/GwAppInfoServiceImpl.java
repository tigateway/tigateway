package ti.gateway.dashboard.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ti.gateway.dashboard.base.Page;
import ti.gateway.dashboard.service.IGwAppInfoService;
import ti.gateway.dashboard.service.dto.GwAppInfnDto;
import ti.gateway.dashboard.service.entity.GwAppInfoEt;
import ti.gateway.storage.db.mapper.GwAppInfoMapper;
import ti.gateway.storage.db.model.GwAppInfo;
import ti.gateway.util.BeanCopierUtils;

import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class GwAppInfoServiceImpl implements IGwAppInfoService {

    @Resource
    private GwAppInfoMapper gwAppInfoMapper;

    @Override
    public Page<GwAppInfoEt> queryGwAppInfos(GwAppInfnDto gwAppInfnDto, int currentPage, int pageSize) {
        Page<GwAppInfoEt> pager = null;
        GwAppInfo gwAppInfo = BeanCopierUtils.copierTargetBean(gwAppInfnDto, GwAppInfnDto.class, GwAppInfo.class);
        int sum = gwAppInfoMapper.selectCountWithBySearch(gwAppInfo);
        if (sum > 0) {
            pager = new Page(sum, currentPage, pageSize);
            List<GwAppInfo> gwAppInfos = gwAppInfoMapper.selectListWithBySearch(gwAppInfo, pager.getStart(), pager.getPageSize());
            List<GwAppInfoEt> gwAppInfoEts = BeanCopierUtils.copierTargetBeanList(gwAppInfos, GwAppInfo.class, GwAppInfoEt.class);
            pager.setList(gwAppInfoEts);
        } else {
            pager = new Page();
        }
        return pager;
    }

}
