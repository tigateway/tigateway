package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecExtensionsFilters {
    public static final String SERIALIZED_NAME_API_KEY = "apiKey";
    @SerializedName("apiKey")
    private V1SpringCloudGatewaySpecExtensionsFiltersApiKey apiKey;
    public static final String SERIALIZED_NAME_JWT_KEY = "jwtKey";
    @SerializedName("jwtKey")
    private V1SpringCloudGatewaySpecExtensionsFiltersJwtKey jwtKey;

    public V1SpringCloudGatewaySpecExtensionsFilters() {
    }

    public V1SpringCloudGatewaySpecExtensionsFilters apiKey(V1SpringCloudGatewaySpecExtensionsFiltersApiKey apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecExtensionsFiltersApiKey getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(V1SpringCloudGatewaySpecExtensionsFiltersApiKey apiKey) {
        this.apiKey = apiKey;
    }

    public V1SpringCloudGatewaySpecExtensionsFilters jwtKey(V1SpringCloudGatewaySpecExtensionsFiltersJwtKey jwtKey) {
        this.jwtKey = jwtKey;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecExtensionsFiltersJwtKey getJwtKey() {
        return this.jwtKey;
    }

    public void setJwtKey(V1SpringCloudGatewaySpecExtensionsFiltersJwtKey jwtKey) {
        this.jwtKey = jwtKey;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecExtensionsFilters v1SpringCloudGatewaySpecExtensionsFilters = (V1SpringCloudGatewaySpecExtensionsFilters)o;
            return Objects.equals(this.apiKey, v1SpringCloudGatewaySpecExtensionsFilters.apiKey) && Objects.equals(this.jwtKey, v1SpringCloudGatewaySpecExtensionsFilters.jwtKey);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.apiKey, this.jwtKey});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecExtensionsFilters {\n");
        sb.append("    apiKey: ").append(this.toIndentedString(this.apiKey)).append("\n");
        sb.append("    jwtKey: ").append(this.toIndentedString(this.jwtKey)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

