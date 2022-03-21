package io.pivotal.spring.cloud.gateway.body;

import io.pivotal.spring.cloud.gateway.core.KeyValue;
import io.pivotal.spring.cloud.gateway.core.KeyValueConfig;
import io.pivotal.spring.cloud.gateway.core.KeyValueGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

public class RewriteResponseBodyGatewayFilterFactory extends KeyValueGatewayFilterFactory {
    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;

    public RewriteResponseBodyGatewayFilterFactory(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory) {
        this.modifyResponseBodyGatewayFilterFactory = modifyResponseBodyGatewayFilterFactory;
    }

    @Override
    public GatewayFilter apply(KeyValueConfig config) {
        ModifyResponseBodyGatewayFilterFactory.Config modifyResponseBodyConfig = new ModifyResponseBodyGatewayFilterFactory.Config();
        modifyResponseBodyConfig.setInClass(String.class);
        modifyResponseBodyConfig.setOutClass(String.class);
        RewriteFunction<String, String> rewriteFunction = (exchange, body) -> {
            KeyValue[] keyValues = config.getKeyValues();
            int length = keyValues.length;

            for (int i = 0; i < length; ++i) {
                KeyValue kv = keyValues[i];
                if (!StringUtils.hasText(body)) {
                    return Mono.empty();
                }

                body = body.replaceAll(kv.getKey(), kv.getValue());
            }

            return Mono.just(body);
        };

        modifyResponseBodyConfig.setRewriteFunction(rewriteFunction);

        return this.modifyResponseBodyGatewayFilterFactory.apply(modifyResponseBodyConfig);
    }
}
