package ti.gateway.admin.storage.db.mapper;

import ti.gateway.admin.storage.db.model.GwAppServer;

import java.util.Set;

@Mybatis
public interface GwAppServerMapper {

    int deleteByPrimaryKey(Long id);

    int insert(GwAppServer record);

    int insertSelective(GwAppServer record);

    GwAppServer selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(GwAppServer record);

    int updateByPrimaryKey(GwAppServer record);

    Set<GwAppServer> selectListByAppkey(String appKey);

}
