package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayRouteConfigSpecOpenapi {
    public static final String SERIALIZED_NAME_COMPONENTS = "components";
    @SerializedName("components")
    private Object components;

    public V1SpringCloudGatewayRouteConfigSpecOpenapi() {
    }

    public V1SpringCloudGatewayRouteConfigSpecOpenapi components(Object components) {
        this.components = components;
        return this;
    }

    @Nullable
    @ApiModelProperty("Holds a set of reusable objects for different aspects of the OAS, as defined by https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.0.1.md#componentsObject")
    public Object getComponents() {
        return this.components;
    }

    public void setComponents(Object components) {
        this.components = components;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayRouteConfigSpecOpenapi v1SpringCloudGatewayRouteConfigSpecOpenapi = (V1SpringCloudGatewayRouteConfigSpecOpenapi)o;
            return Objects.equals(this.components, v1SpringCloudGatewayRouteConfigSpecOpenapi.components);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.components});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayRouteConfigSpecOpenapi {\n");
        sb.append("    components: ").append(this.toIndentedString(this.components)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
