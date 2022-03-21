package io.pivotal.spring.cloud.gateway.tls;

import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationPropertiesBinding
class TlsSecretConverter implements Converter<String, TlsSecret> {
    TlsSecretConverter() {
    }

    public TlsSecret convert(String secretPath) {
        return new TlsSecret(secretPath);
    }
}
