package ti.gateway.ainative.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ti.gateway.ainative.model.LlmResponse;

/**
 * AI原生网关自动配置类
 * 
 * 提供AI原生网关的自动配置功能
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@Configuration
@ConditionalOnProperty(prefix = "tigateway.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
@Import({
    WebFluxConfig.class,
    GatewayConfig.class
})
public class AiNativeAutoConfiguration {

    /**
     * 配置ReactiveRedisTemplate用于LLM缓存
     */
    @Bean
    @ConditionalOnClass(ReactiveRedisConnectionFactory.class)
    public ReactiveRedisTemplate<String, Object> llmCacheRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory) {
        
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
        
        RedisSerializationContext<String, Object> serializationContext = 
            RedisSerializationContext.<String, Object>newSerializationContext()
                .key(stringSerializer)
                .value(jsonSerializer)
                .hashKey(stringSerializer)
                .hashValue(jsonSerializer)
                .build();
        
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }
}
