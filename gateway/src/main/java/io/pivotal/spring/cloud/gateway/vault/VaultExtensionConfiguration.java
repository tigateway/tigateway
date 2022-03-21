package io.pivotal.spring.cloud.gateway.vault;

import com.vmware.tanzu.springcloudgateway.SecretsProvidersLocator;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(
        value = {"extensions.vault.enabled"},
        havingValue = "true"
)
public class VaultExtensionConfiguration {
    public VaultExtensionConfiguration() {
    }

    @Bean
    SecretsProvidersLocator secretsProvidersLocator(@Value("${extensions.vault.paths}") List<Path> secretsPath, Clock clock) {
        return new SecretsProvidersLocator(secretsPath, clock);
    }
}
