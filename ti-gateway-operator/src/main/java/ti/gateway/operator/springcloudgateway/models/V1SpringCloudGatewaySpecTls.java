package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecTls {
    public static final String SERIALIZED_NAME_HOSTS = "hosts";
    @SerializedName("hosts")
    private List<String> hosts = null;
    public static final String SERIALIZED_NAME_SECRET_NAME = "secretName";
    @SerializedName("secretName")
    private String secretName;

    public V1SpringCloudGatewaySpecTls() {
    }

    public V1SpringCloudGatewaySpecTls hosts(List<String> hosts) {
        this.hosts = hosts;
        return this;
    }

    public V1SpringCloudGatewaySpecTls addHostsItem(String hostsItem) {
        if (this.hosts == null) {
            this.hosts = new ArrayList();
        }

        this.hosts.add(hostsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public List<String> getHosts() {
        return this.hosts;
    }

    public void setHosts(List<String> hosts) {
        this.hosts = hosts;
    }

    public V1SpringCloudGatewaySpecTls secretName(String secretName) {
        this.secretName = secretName;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getSecretName() {
        return this.secretName;
    }

    public void setSecretName(String secretName) {
        this.secretName = secretName;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecTls v1SpringCloudGatewaySpecTls = (V1SpringCloudGatewaySpecTls)o;
            return Objects.equals(this.hosts, v1SpringCloudGatewaySpecTls.hosts) && Objects.equals(this.secretName, v1SpringCloudGatewaySpecTls.secretName);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.hosts, this.secretName});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecTls {\n");
        sb.append("    hosts: ").append(this.toIndentedString(this.hosts)).append("\n");
        sb.append("    secretName: ").append(this.toIndentedString(this.secretName)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
