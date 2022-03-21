package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecExtensions {
    public static final String SERIALIZED_NAME_CUSTOM = "custom";
    @SerializedName("custom")
    private List<String> custom = null;
    public static final String SERIALIZED_NAME_FILTERS = "filters";
    @SerializedName("filters")
    private V1SpringCloudGatewaySpecExtensionsFilters filters;
    public static final String SERIALIZED_NAME_SECRETS_PROVIDERS = "secretsProviders";
    @SerializedName("secretsProviders")
    private List<V1SpringCloudGatewaySpecExtensionsSecretsProviders> secretsProviders = null;

    public V1SpringCloudGatewaySpecExtensions() {
    }

    public V1SpringCloudGatewaySpecExtensions custom(List<String> custom) {
        this.custom = custom;
        return this;
    }

    public V1SpringCloudGatewaySpecExtensions addCustomItem(String customItem) {
        if (this.custom == null) {
            this.custom = new ArrayList();
        }

        this.custom.add(customItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("Array of custom extensions to load (name must match the ConfigMap or PersistentVolumeClaim name) ")
    public List<String> getCustom() {
        return this.custom;
    }

    public void setCustom(List<String> custom) {
        this.custom = custom;
    }

    public V1SpringCloudGatewaySpecExtensions filters(V1SpringCloudGatewaySpecExtensionsFilters filters) {
        this.filters = filters;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecExtensionsFilters getFilters() {
        return this.filters;
    }

    public void setFilters(V1SpringCloudGatewaySpecExtensionsFilters filters) {
        this.filters = filters;
    }

    public V1SpringCloudGatewaySpecExtensions secretsProviders(List<V1SpringCloudGatewaySpecExtensionsSecretsProviders> secretsProviders) {
        this.secretsProviders = secretsProviders;
        return this;
    }

    public V1SpringCloudGatewaySpecExtensions addSecretsProvidersItem(V1SpringCloudGatewaySpecExtensionsSecretsProviders secretsProvidersItem) {
        if (this.secretsProviders == null) {
            this.secretsProviders = new ArrayList();
        }

        this.secretsProviders.add(secretsProvidersItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("Array of secret providers. These are identified by a name and follow conventions similar to `volumes`. Currently supported Vault. ")
    public List<V1SpringCloudGatewaySpecExtensionsSecretsProviders> getSecretsProviders() {
        return this.secretsProviders;
    }

    public void setSecretsProviders(List<V1SpringCloudGatewaySpecExtensionsSecretsProviders> secretsProviders) {
        this.secretsProviders = secretsProviders;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecExtensions v1SpringCloudGatewaySpecExtensions = (V1SpringCloudGatewaySpecExtensions)o;
            return Objects.equals(this.custom, v1SpringCloudGatewaySpecExtensions.custom) && Objects.equals(this.filters, v1SpringCloudGatewaySpecExtensions.filters) && Objects.equals(this.secretsProviders, v1SpringCloudGatewaySpecExtensions.secretsProviders);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.custom, this.filters, this.secretsProviders});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecExtensions {\n");
        sb.append("    custom: ").append(this.toIndentedString(this.custom)).append("\n");
        sb.append("    filters: ").append(this.toIndentedString(this.filters)).append("\n");
        sb.append("    secretsProviders: ").append(this.toIndentedString(this.secretsProviders)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

