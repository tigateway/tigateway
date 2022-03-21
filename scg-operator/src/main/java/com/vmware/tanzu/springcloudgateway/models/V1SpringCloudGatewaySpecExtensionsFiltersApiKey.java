package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

@ApiModel(
        description = "API Key specific configurations "
)
public class V1SpringCloudGatewaySpecExtensionsFiltersApiKey {
    public static final String SERIALIZED_NAME_ENABLED = "enabled";
    @SerializedName("enabled")
    private Boolean enabled;
    public static final String SERIALIZED_NAME_SECRETS_PROVIDER_NAME = "secretsProviderName";
    @SerializedName("secretsProviderName")
    private String secretsProviderName;

    public V1SpringCloudGatewaySpecExtensionsFiltersApiKey() {
    }

    public V1SpringCloudGatewaySpecExtensionsFiltersApiKey enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public Boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public V1SpringCloudGatewaySpecExtensionsFiltersApiKey secretsProviderName(String secretsProviderName) {
        this.secretsProviderName = secretsProviderName;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getSecretsProviderName() {
        return this.secretsProviderName;
    }

    public void setSecretsProviderName(String secretsProviderName) {
        this.secretsProviderName = secretsProviderName;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecExtensionsFiltersApiKey v1SpringCloudGatewaySpecExtensionsFiltersApiKey = (V1SpringCloudGatewaySpecExtensionsFiltersApiKey)o;
            return Objects.equals(this.enabled, v1SpringCloudGatewaySpecExtensionsFiltersApiKey.enabled) && Objects.equals(this.secretsProviderName, v1SpringCloudGatewaySpecExtensionsFiltersApiKey.secretsProviderName);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.enabled, this.secretsProviderName});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecExtensionsFiltersApiKey {\n");
        sb.append("    enabled: ").append(this.toIndentedString(this.enabled)).append("\n");
        sb.append("    secretsProviderName: ").append(this.toIndentedString(this.secretsProviderName)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

