package io.pivotal.spring.cloud.gateway.body.json;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import com.jayway.jsonpath.Predicate;
import io.pivotal.spring.cloud.gateway.core.KeyValue;
import io.pivotal.spring.cloud.gateway.core.KeyValueConfig;
import io.pivotal.spring.cloud.gateway.core.KeyValueGatewayFilterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyResponseBodyGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class RewriteJsonAttributesResponseBodyGatewayFilterFactory extends KeyValueGatewayFilterFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(RewriteJsonAttributesResponseBodyGatewayFilterFactory.class);
    private final ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory;

    public RewriteJsonAttributesResponseBodyGatewayFilterFactory(ModifyResponseBodyGatewayFilterFactory modifyResponseBodyGatewayFilterFactory) {
        this.modifyResponseBodyGatewayFilterFactory = modifyResponseBodyGatewayFilterFactory;
    }

    @Override
    public GatewayFilter apply(KeyValueConfig config) {
        ModifyResponseBodyGatewayFilterFactory.Config modifyResponseBodyConfig = new ModifyResponseBodyGatewayFilterFactory.Config();
        modifyResponseBodyConfig.setInClass(String.class);
        modifyResponseBodyConfig.setOutClass(String.class);
        RewriteFunction<String, String> rewriteFunction = (exchange, body) -> {
            DocumentContext jsonBody = JsonPath.parse(body);
            KeyValue[] keyValues = config.getKeyValues();
            int length = keyValues.length;

            for (int i = 0; i < length; ++i) {
                KeyValue kv = keyValues[i];

                try {
                    jsonBody.set("$." + kv.getKey(), kv.getValue(), new Predicate[0]);
                } catch (PathNotFoundException exception) {
                    LOGGER.debug("Could not set json path: " + kv.getKey(), exception.getMessage());
                }
            }

            return Mono.just(jsonBody.jsonString());
        };

        modifyResponseBodyConfig.setRewriteFunction(rewriteFunction);

        return this.modifyResponseBodyGatewayFilterFactory.apply(modifyResponseBodyConfig);
    }
}
