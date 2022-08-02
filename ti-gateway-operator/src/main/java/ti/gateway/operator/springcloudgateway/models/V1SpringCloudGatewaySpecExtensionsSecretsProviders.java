package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecExtensionsSecretsProviders {
    public static final String SERIALIZED_NAME_NAME = "name";
    @SerializedName("name")
    private String name;
    public static final String SERIALIZED_NAME_VAULT = "vault";
    @SerializedName("vault")
    private V1SpringCloudGatewaySpecExtensionsVault vault;

    public V1SpringCloudGatewaySpecExtensionsSecretsProviders() {
    }

    public V1SpringCloudGatewaySpecExtensionsSecretsProviders name(String name) {
        this.name = name;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public V1SpringCloudGatewaySpecExtensionsSecretsProviders vault(V1SpringCloudGatewaySpecExtensionsVault vault) {
        this.vault = vault;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecExtensionsVault getVault() {
        return this.vault;
    }

    public void setVault(V1SpringCloudGatewaySpecExtensionsVault vault) {
        this.vault = vault;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecExtensionsSecretsProviders v1SpringCloudGatewaySpecExtensionsSecretsProviders = (V1SpringCloudGatewaySpecExtensionsSecretsProviders)o;
            return Objects.equals(this.name, v1SpringCloudGatewaySpecExtensionsSecretsProviders.name) && Objects.equals(this.vault, v1SpringCloudGatewaySpecExtensionsSecretsProviders.vault);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.vault});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecExtensionsSecretsProviders {\n");
        sb.append("    name: ").append(this.toIndentedString(this.name)).append("\n");
        sb.append("    vault: ").append(this.toIndentedString(this.vault)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
