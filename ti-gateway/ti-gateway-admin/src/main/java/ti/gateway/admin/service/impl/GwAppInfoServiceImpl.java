package ti.gateway.admin.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ti.gateway.admin.service.entity.GwAppInfoEntity;
import ti.gateway.admin.base.Page;
import ti.gateway.admin.service.IGwAppInfoService;
import ti.gateway.admin.service.dto.GwAppInfoDto;
import ti.gateway.base.storage.configmap.ConfigMapAppInfoRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GwAppInfoServiceImpl implements IGwAppInfoService {

    @Autowired
    private ConfigMapAppInfoRepository configMapAppInfoRepository;

    @Override
    public Page<GwAppInfoEntity> queryGwAppInfos(GwAppInfoDto gwAppInfoDto, int currentPage, int pageSize) {
        try {
            // 从ConfigMap获取应用信息
            List<ti.gateway.base.storage.configmap.model.ConfigMapAppInfo> configMapApps = 
                configMapAppInfoRepository.findAll();
            
            // 转换为Entity
            List<GwAppInfoEntity> entities = configMapApps.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
            
            // 创建分页对象
            Page<GwAppInfoEntity> page = new Page<>(entities.size(), currentPage, pageSize);
            page.setList(entities);
            
            return page;
        } catch (Exception e) {
            // 如果获取失败，返回空分页
            return new Page<>();
        }
    }
    
    private GwAppInfoEntity convertToEntity(ti.gateway.base.storage.configmap.model.ConfigMapAppInfo configMapApp) {
        GwAppInfoEntity entity = new GwAppInfoEntity();
        entity.setName(configMapApp.getName());
        entity.setDesc(configMapApp.getDesc());
        entity.setType(configMapApp.getType());
        entity.setAppKey(configMapApp.getAppKey());
        entity.setAppSecret(configMapApp.getAppSecret());
        entity.setStatus(configMapApp.getStatus());
        entity.setCtime(java.sql.Timestamp.valueOf(configMapApp.getCtime()));
        entity.setMtime(java.sql.Timestamp.valueOf(configMapApp.getMtime()));
        return entity;
    }

}
