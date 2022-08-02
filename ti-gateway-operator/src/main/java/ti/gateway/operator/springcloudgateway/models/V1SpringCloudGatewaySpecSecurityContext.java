package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

@ApiModel(
        description = "SecurityContext applied to the Gateway pod(s). "
)
public class V1SpringCloudGatewaySpecSecurityContext {
    public static final String SERIALIZED_NAME_FS_GROUP = "fsGroup";
    @SerializedName("fsGroup")
    private Integer fsGroup;
    public static final String SERIALIZED_NAME_RUN_AS_GROUP = "runAsGroup";
    @SerializedName("runAsGroup")
    private Integer runAsGroup;
    public static final String SERIALIZED_NAME_RUN_AS_USER = "runAsUser";
    @SerializedName("runAsUser")
    private Integer runAsUser;

    public V1SpringCloudGatewaySpecSecurityContext() {
    }

    public V1SpringCloudGatewaySpecSecurityContext fsGroup(Integer fsGroup) {
        this.fsGroup = fsGroup;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public Integer getFsGroup() {
        return this.fsGroup;
    }

    public void setFsGroup(Integer fsGroup) {
        this.fsGroup = fsGroup;
    }

    public V1SpringCloudGatewaySpecSecurityContext runAsGroup(Integer runAsGroup) {
        this.runAsGroup = runAsGroup;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public Integer getRunAsGroup() {
        return this.runAsGroup;
    }

    public void setRunAsGroup(Integer runAsGroup) {
        this.runAsGroup = runAsGroup;
    }

    public V1SpringCloudGatewaySpecSecurityContext runAsUser(Integer runAsUser) {
        this.runAsUser = runAsUser;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public Integer getRunAsUser() {
        return this.runAsUser;
    }

    public void setRunAsUser(Integer runAsUser) {
        this.runAsUser = runAsUser;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecSecurityContext v1SpringCloudGatewaySpecSecurityContext = (V1SpringCloudGatewaySpecSecurityContext)o;
            return Objects.equals(this.fsGroup, v1SpringCloudGatewaySpecSecurityContext.fsGroup) && Objects.equals(this.runAsGroup, v1SpringCloudGatewaySpecSecurityContext.runAsGroup) && Objects.equals(this.runAsUser, v1SpringCloudGatewaySpecSecurityContext.runAsUser);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.fsGroup, this.runAsGroup, this.runAsUser});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecSecurityContext {\n");
        sb.append("    fsGroup: ").append(this.toIndentedString(this.fsGroup)).append("\n");
        sb.append("    runAsGroup: ").append(this.toIndentedString(this.runAsGroup)).append("\n");
        sb.append("    runAsUser: ").append(this.toIndentedString(this.runAsUser)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

