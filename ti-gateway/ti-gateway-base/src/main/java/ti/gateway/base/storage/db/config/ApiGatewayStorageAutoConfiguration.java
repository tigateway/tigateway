package ti.gateway.base.storage.db.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ti.gateway.base.core.cache.AppServerStorage;
import ti.gateway.base.storage.db.DbAppServerStorage;
import ti.gateway.base.storage.db.mapper.TigaAppInfoMapper;
import ti.gateway.base.storage.db.mapper.TigaAppServerMapper;

/**
 * api gateway storage
 */
@Configuration
@ConditionalOnClass(AppServerStorage.class)
public class ApiGatewayStorageAutoConfiguration {

    @Bean
    public AppServerStorage appServerStorage(TigaAppInfoMapper gwAppInfoMapper, TigaAppServerMapper gwAppServerMapper) {
        return new DbAppServerStorage(gwAppInfoMapper, gwAppServerMapper);
    }

}
