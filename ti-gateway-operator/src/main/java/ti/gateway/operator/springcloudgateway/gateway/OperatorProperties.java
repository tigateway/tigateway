package ti.gateway.operator.springcloudgateway.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConfigurationProperties("operator")
@ConstructorBinding
public class OperatorProperties {
    private final String gatewayImageName;
    private final String imagePullSecretName;
    private final String installNamespace;

    public OperatorProperties(String gatewayImageName, String imagePullSecretName, String installNamespace) {
        this.gatewayImageName = gatewayImageName;
        this.imagePullSecretName = imagePullSecretName;
        this.installNamespace = installNamespace;
    }

    public String getInstallNamespace() {
        return this.installNamespace;
    }

    public String getGatewayImageName() {
        return this.gatewayImageName;
    }

    public String getImagePullSecretName() {
        return this.imagePullSecretName;
    }
}

