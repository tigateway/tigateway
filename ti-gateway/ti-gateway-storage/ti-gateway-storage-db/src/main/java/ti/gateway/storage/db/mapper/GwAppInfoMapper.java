package ti.gateway.storage.db.mapper;

import org.apache.ibatis.annotations.Param;
import ti.gateway.storage.db.model.GwAppInfo;

import java.util.List;
import java.util.Set;

@Mybatis
public interface GwAppInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(GwAppInfo record);

    int insertSelective(GwAppInfo record);

    GwAppInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(GwAppInfo record);

    int updateByPrimaryKey(GwAppInfo record);

    Set<String> selectAllAppkeys();

    /**
     * 通过Key获取服务详情
     *
     * @param appKey
     * @return
     */
    GwAppInfo selectByAppkey(String appKey);

    int selectCountWithBySearch(@Param("gwAppInfo") GwAppInfo gwAppInfo);

    List<GwAppInfo> selectListWithBySearch(@Param("gwAppInfo") GwAppInfo gwAppInfo, @Param("start") int start, @Param("pageSize") int pageSize);

}
