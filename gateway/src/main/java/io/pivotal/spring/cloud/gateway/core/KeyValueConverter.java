package io.pivotal.spring.cloud.gateway.core;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KeyValueConverter implements Converter<String, KeyValue> {
    public KeyValueConverter() {
    }

    public KeyValue convert(String source) throws IllegalArgumentException {
        try {
            String[] split = source.split(":");
            if (source.contains(":") && StringUtils.hasText(split[0])) {
                return new KeyValue(split[0], split.length == 1 ? "" : split[1]);
            } else {
                throw new IllegalArgumentException("Invalid configuration, expected format is: 'key:value'");
            }
        } catch (ArrayIndexOutOfBoundsException exception) {
            throw new IllegalArgumentException("Invalid configuration, expected format is: 'key:value'");
        }
    }
}
