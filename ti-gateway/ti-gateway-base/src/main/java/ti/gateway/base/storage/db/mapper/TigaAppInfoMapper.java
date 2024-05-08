package ti.gateway.base.storage.db.mapper;

import org.apache.ibatis.annotations.Param;
import ti.gateway.base.storage.db.model.TigaAppInfo;

import java.util.List;
import java.util.Set;

@Mybatis
public interface TigaAppInfoMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TigaAppInfo record);

    int insertSelective(TigaAppInfo record);

    TigaAppInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TigaAppInfo record);

    int updateByPrimaryKey(TigaAppInfo record);

    Set<String> selectAllAppkeys();

    /**
     * 通过Key获取服务详情
     *
     * @param appKey appKey
     * @return 服务详情
     */
    TigaAppInfo selectByAppkey(String appKey);

    int selectCountWithBySearch(@Param("gwAppInfo") TigaAppInfo gwAppInfo);

    List<TigaAppInfo> selectListWithBySearch(@Param("gwAppInfo") TigaAppInfo gwAppInfo, @Param("start") int start, @Param("pageSize") int pageSize);

}
