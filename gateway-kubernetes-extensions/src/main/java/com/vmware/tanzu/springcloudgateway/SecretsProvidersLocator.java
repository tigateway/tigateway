package com.vmware.tanzu.springcloudgateway;

import java.nio.file.Path;
import java.time.Clock;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecretsProvidersLocator {
    private final Logger log = LoggerFactory.getLogger(SecretsProvidersLocator.class);
    private final Clock clock;
    private final Map<String, SecretsProvider> providers = new HashMap<>();
    private static final List<String> RESERVED_PATHS = List.of("api-portal-for-vmware-tanzu", "jwt-keys-for-vmware-tanzu");

    public SecretsProvidersLocator(List<Path> secretsPaths, Clock clock) {
        this.clock = clock;
        List<Path> paths = (List<Path>)secretsPaths.stream().filter((path) -> {
            return !this.containsAny(path, RESERVED_PATHS);
        }).collect(Collectors.toList());
        paths.forEach((path) -> {
            String providerKey = path.getFileName().toString();
            if (!this.providers.containsKey(providerKey)) {
                this.providers.put(providerKey, new SecretsProvider(new LocalVaultFileReader(path, clock)));
            } else {
                this.log.info("Ignoring duplicated Secret Provider name: " + providerKey);
            }

        });
    }

    private boolean containsAny(Path path, List<String> reservedPaths) {
        return reservedPaths.stream().anyMatch((reservedPath) -> {
            return path.toAbsolutePath().toString().contains(reservedPath);
        });
    }

    public SecretsProvider getSecretProvider(String name) {
        return (SecretsProvider)this.providers.get(name);
    }
}
