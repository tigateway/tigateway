package ti.gateway.kubernetes.info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.Map;

@Component
public class ApiKeyInfoContributor implements InfoContributor {
    @Value("${apiKey.enabled:false}")
    boolean apiKeyEnabled;
    @Value("${apiKey.keysFilePath:}")
    String keysFilePath;

    public ApiKeyInfoContributor() {
    }

    @Override
    public void contribute(Info.Builder builder) {
        builder.withDetail("apikey", Map.of(
                "enabled", this.apiKeyEnabled,
                "loaded", Files.exists(Paths.get(this.keysFilePath), new LinkOption[0])
                ));
    }
}
