package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

@ApiModel(
        description = "Vault integration configuration parameters "
)
public class V1SpringCloudGatewaySpecExtensionsVault {
    public static final String SERIALIZED_NAME_AUTH_PATH = "authPath";
    @SerializedName("authPath")
    private String authPath;
    public static final String SERIALIZED_NAME_PATH = "path";
    @SerializedName("path")
    private String path;
    public static final String SERIALIZED_NAME_ROLE_NAME = "roleName";
    @SerializedName("roleName")
    private String roleName;

    public V1SpringCloudGatewaySpecExtensionsVault() {
    }

    public V1SpringCloudGatewaySpecExtensionsVault authPath(String authPath) {
        this.authPath = authPath;
        return this;
    }

    @Nullable
    @ApiModelProperty("Authentication path for the Kubernetes auth method.")
    public String getAuthPath() {
        return this.authPath;
    }

    public void setAuthPath(String authPath) {
        this.authPath = authPath;
    }

    public V1SpringCloudGatewaySpecExtensionsVault path(String path) {
        this.path = path;
        return this;
    }

    @Nullable
    @ApiModelProperty("Vault secrets' path (e.g. 'my-secrets/context').")
    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public V1SpringCloudGatewaySpecExtensionsVault roleName(String roleName) {
        this.roleName = roleName;
        return this;
    }

    @Nullable
    @ApiModelProperty("Vault rolename with access to the secrets according to the Vault policies.")
    public String getRoleName() {
        return this.roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecExtensionsVault v1SpringCloudGatewaySpecExtensionsVault = (V1SpringCloudGatewaySpecExtensionsVault)o;
            return Objects.equals(this.authPath, v1SpringCloudGatewaySpecExtensionsVault.authPath) && Objects.equals(this.path, v1SpringCloudGatewaySpecExtensionsVault.path) && Objects.equals(this.roleName, v1SpringCloudGatewaySpecExtensionsVault.roleName);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.authPath, this.path, this.roleName});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecExtensionsVault {\n");
        sb.append("    authPath: ").append(this.toIndentedString(this.authPath)).append("\n");
        sb.append("    path: ").append(this.toIndentedString(this.path)).append("\n");
        sb.append("    roleName: ").append(this.toIndentedString(this.roleName)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
