package ti.gateway.base.storage.configmap;

import ti.gateway.base.storage.configmap.model.ConfigMapAppInfo;

import java.util.List;
import java.util.Set;

/**
 * ConfigMap应用信息仓库接口
 */
public interface ConfigMapAppInfoRepository {

    /**
     * 根据应用Key获取应用信息
     *
     * @param appKey 应用Key
     * @return 应用信息
     */
    ConfigMapAppInfo findByAppKey(String appKey);

    /**
     * 获取所有应用Key
     *
     * @return 应用Key集合
     */
    Set<String> findAllAppKeys();

    /**
     * 保存应用信息
     *
     * @param appInfo 应用信息
     * @return 是否保存成功
     */
    boolean save(ConfigMapAppInfo appInfo);

    /**
     * 删除应用信息
     *
     * @param appKey 应用Key
     * @return 是否删除成功
     */
    boolean delete(String appKey);

    /**
     * 获取所有应用信息
     *
     * @return 应用信息列表
     */
    List<ConfigMapAppInfo> findAll();

    /**
     * 刷新缓存
     */
    void refreshCache();

    /**
     * 检查ConfigMap是否存在
     *
     * @return 是否存在
     */
    boolean configMapExists();
}
