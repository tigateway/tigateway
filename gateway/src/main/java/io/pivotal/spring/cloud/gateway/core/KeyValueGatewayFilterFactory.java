package io.pivotal.spring.cloud.gateway.core;

import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;

import java.util.Collections;
import java.util.List;

public abstract class KeyValueGatewayFilterFactory extends AbstractGatewayFilterFactory<KeyValueConfig> {

    public KeyValueGatewayFilterFactory() {
    }

    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("keyValues");
    }

    public KeyValueConfig newConfig() {
        return new KeyValueConfig();
    }

    public Class<KeyValueConfig> getConfigClass() {
        return KeyValueConfig.class;
    }
}
