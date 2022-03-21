package io.pivotal.spring.cloud.gateway.basicauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractNameValueGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.AddRequestHeaderGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class BasicAuthGatewayFilterFactory implements GatewayFilterFactory<BasicAuthenticationProperties> {
    private final Logger LOGGER = LoggerFactory.getLogger(BasicAuthGatewayFilterFactory.class);
    private final AddRequestHeaderGatewayFilterFactory addRequestHeaderGatewayFilterFactory = new AddRequestHeaderGatewayFilterFactory();

    public BasicAuthGatewayFilterFactory() {
    }

    private AbstractNameValueGatewayFilterFactory.NameValueConfig getAuthHeaderConfig(String encodedCredentials) {
        return (new AbstractNameValueGatewayFilterFactory.NameValueConfig())
                .setName("Authorization").setValue("Basic " + encodedCredentials);
    }

    @Override
    public Class<BasicAuthenticationProperties> getConfigClass() {
        return BasicAuthenticationProperties.class;
    }

    @Override
    public BasicAuthenticationProperties newConfig() {
        return new BasicAuthenticationProperties();
    }

    @Override
    public GatewayFilter apply(BasicAuthenticationProperties config) {
        return this.addRequestHeaderGatewayFilterFactory.apply(this.getAuthHeaderConfig(config.getEncodedCredentials()));
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("encodedCredentials");
    }
}
