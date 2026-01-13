package ti.gateway.kubernetes.core;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class KeyValueConverter implements Converter<String, KeyValue> {
    public KeyValueConverter() {
    }

    public KeyValue convert(String source) throws IllegalArgumentException {
        try {
            int colonIndex = source.indexOf(':');
            if (colonIndex >= 0 && StringUtils.hasText(source.substring(0, colonIndex))) {
                String key = source.substring(0, colonIndex);
                String value = colonIndex < source.length() - 1 ? source.substring(colonIndex + 1) : "";
                return new KeyValue(key, value);
            } else {
                throw new IllegalArgumentException("Invalid configuration, expected format is: 'key:value'");
            }
        } catch (StringIndexOutOfBoundsException exception) {
            throw new IllegalArgumentException("Invalid configuration, expected format is: 'key:value'");
        }
    }
}
