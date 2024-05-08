package ti.gateway.base.storage.db.mapper;

import ti.gateway.base.storage.db.model.TigaAppServer;

import java.util.Set;

@Mybatis
public interface TigaAppServerMapper {

    int deleteByPrimaryKey(Long id);

    int insert(TigaAppServer record);

    int insertSelective(TigaAppServer record);

    TigaAppServer selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TigaAppServer record);

    int updateByPrimaryKey(TigaAppServer record);

    Set<TigaAppServer> selectListByAppkey(String appKey);

}
