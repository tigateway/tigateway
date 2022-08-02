package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayMappingSpecGatewayRef {
    public static final String SERIALIZED_NAME_NAME = "name";
    @SerializedName("name")
    private String name;
    public static final String SERIALIZED_NAME_NAMESPACE = "namespace";
    @SerializedName("namespace")
    private String namespace;

    public V1SpringCloudGatewayMappingSpecGatewayRef() {
    }

    public V1SpringCloudGatewayMappingSpecGatewayRef name(String name) {
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

    public V1SpringCloudGatewayMappingSpecGatewayRef namespace(String namespace) {
        this.namespace = namespace;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getNamespace() {
        return this.namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayMappingSpecGatewayRef v1SpringCloudGatewayMappingSpecGatewayRef = (V1SpringCloudGatewayMappingSpecGatewayRef)o;
            return Objects.equals(this.name, v1SpringCloudGatewayMappingSpecGatewayRef.name) && Objects.equals(this.namespace, v1SpringCloudGatewayMappingSpecGatewayRef.namespace);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.name, this.namespace});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayMappingSpecGatewayRef {\n");
        sb.append("    name: ").append(this.toIndentedString(this.name)).append("\n");
        sb.append("    namespace: ").append(this.toIndentedString(this.namespace)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

