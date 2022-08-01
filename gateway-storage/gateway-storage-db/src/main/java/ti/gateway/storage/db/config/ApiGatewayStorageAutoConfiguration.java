package ti.gateway.storage.db.config;


import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ti.gateway.core.cache.AppServerStorage;
import ti.gateway.storage.db.DbAppServerStorage;
import ti.gateway.storage.db.mapper.GwAppInfoMapper;
import ti.gateway.storage.db.mapper.GwAppServerMapper;

/**
 * api gateway storage
 */
@Configuration
@ConditionalOnClass(AppServerStorage.class)
public class ApiGatewayStorageAutoConfiguration {

    @Bean
    public AppServerStorage appServerStorage(GwAppInfoMapper gwAppInfoMapper, GwAppServerMapper gwAppServerMapper) {
        return new DbAppServerStorage(gwAppInfoMapper, gwAppServerMapper);
    }

}
