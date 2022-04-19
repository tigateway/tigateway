package com.vmware.tanzu.springcloudgateway;

import java.util.Map;
import java.util.stream.Collectors;

public class SecretsProvider {
    private final LocalVaultFileReader reader;

    public SecretsProvider(LocalVaultFileReader reader) {
        this.reader = reader;
    }

    public Map<String, Map<String, Object>> getSecrets() {
        return this.reader.getIds().stream()
                .collect(Collectors.toMap((id) -> {
                    return id;
                }, (id) -> {
                    return this.reader.getSecret(id);
                }));
    }

    public Map<String, Object> getSecret(String secretId) {
        return this.reader.getSecret(secretId);
    }
}
