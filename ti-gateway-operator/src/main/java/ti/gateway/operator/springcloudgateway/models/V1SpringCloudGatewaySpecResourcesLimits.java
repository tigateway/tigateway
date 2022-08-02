package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecResourcesLimits {
    public static final String SERIALIZED_NAME_CPU = "cpu";
    @SerializedName("cpu")
    private String cpu;
    public static final String SERIALIZED_NAME_MEMORY = "memory";
    @SerializedName("memory")
    private String memory;

    public V1SpringCloudGatewaySpecResourcesLimits() {
    }

    public V1SpringCloudGatewaySpecResourcesLimits cpu(String cpu) {
        this.cpu = cpu;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getCpu() {
        return this.cpu;
    }

    public void setCpu(String cpu) {
        this.cpu = cpu;
    }

    public V1SpringCloudGatewaySpecResourcesLimits memory(String memory) {
        this.memory = memory;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getMemory() {
        return this.memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecResourcesLimits v1SpringCloudGatewaySpecResourcesLimits = (V1SpringCloudGatewaySpecResourcesLimits)o;
            return Objects.equals(this.cpu, v1SpringCloudGatewaySpecResourcesLimits.cpu) && Objects.equals(this.memory, v1SpringCloudGatewaySpecResourcesLimits.memory);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.cpu, this.memory});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecResourcesLimits {\n");
        sb.append("    cpu: ").append(this.toIndentedString(this.cpu)).append("\n");
        sb.append("    memory: ").append(this.toIndentedString(this.memory)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
