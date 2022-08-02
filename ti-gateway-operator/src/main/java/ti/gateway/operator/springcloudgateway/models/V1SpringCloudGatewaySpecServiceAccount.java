package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

@ApiModel(
        description = "ServiceAccount associated to the Gateway instance "
)
public class V1SpringCloudGatewaySpecServiceAccount {
    public static final String SERIALIZED_NAME_NAME = "name";
    @SerializedName("name")
    private String name;

    public V1SpringCloudGatewaySpecServiceAccount() {
    }

    public V1SpringCloudGatewaySpecServiceAccount name(String name) {
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecServiceAccount v1SpringCloudGatewaySpecServiceAccount = (V1SpringCloudGatewaySpecServiceAccount)o;
            return Objects.equals(this.name, v1SpringCloudGatewaySpecServiceAccount.name);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecServiceAccount {\n");
        sb.append("    name: ").append(this.toIndentedString(this.name)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

