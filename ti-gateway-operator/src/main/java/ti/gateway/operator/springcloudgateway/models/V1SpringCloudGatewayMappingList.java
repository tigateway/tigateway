package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.kubernetes.client.common.KubernetesListObject;
import io.kubernetes.client.openapi.models.V1ListMeta;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

@ApiModel(
        description = "SpringCloudGatewayMappingList is a list of SpringCloudGatewayMapping"
)
public class V1SpringCloudGatewayMappingList implements KubernetesListObject {
    public static final String SERIALIZED_NAME_API_VERSION = "apiVersion";
    @SerializedName("apiVersion")
    private String apiVersion;
    public static final String SERIALIZED_NAME_ITEMS = "items";
    @SerializedName("items")
    private List<V1SpringCloudGatewayMapping> items = new ArrayList<>();
    public static final String SERIALIZED_NAME_KIND = "kind";
    @SerializedName("kind")
    private String kind;
    public static final String SERIALIZED_NAME_METADATA = "metadata";
    @SerializedName("metadata")
    private V1ListMeta metadata = null;

    public V1SpringCloudGatewayMappingList() {
    }

    public V1SpringCloudGatewayMappingList apiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
        return this;
    }

    @Nullable
    @ApiModelProperty("APIVersion defines the versioned schema of this representation of an object. Servers should convert recognized schemas to the latest internal value, and may reject unrecognized values. More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#resources")
    public String getApiVersion() {
        return this.apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public V1SpringCloudGatewayMappingList items(List<V1SpringCloudGatewayMapping> items) {
        this.items = items;
        return this;
    }

    public V1SpringCloudGatewayMappingList addItemsItem(V1SpringCloudGatewayMapping itemsItem) {
        this.items.add(itemsItem);
        return this;
    }

    @ApiModelProperty(
            required = true,
            value = "List of springcloudgatewaymappings. More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md"
    )
    public List<V1SpringCloudGatewayMapping> getItems() {
        return this.items;
    }

    public void setItems(List<V1SpringCloudGatewayMapping> items) {
        this.items = items;
    }

    public V1SpringCloudGatewayMappingList kind(String kind) {
        this.kind = kind;
        return this;
    }

    @Nullable
    @ApiModelProperty("Kind is a string value representing the REST resource this object represents. Servers may infer this from the endpoint the client submits requests to. Cannot be updated. In CamelCase. More info: https://git.k8s.io/community/contributors/devel/sig-architecture/api-conventions.md#types-kinds")
    public String getKind() {
        return this.kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public V1SpringCloudGatewayMappingList metadata(V1ListMeta metadata) {
        this.metadata = metadata;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1ListMeta getMetadata() {
        return this.metadata;
    }

    public void setMetadata(V1ListMeta metadata) {
        this.metadata = metadata;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayMappingList v1SpringCloudGatewayMappingList = (V1SpringCloudGatewayMappingList)o;
            return Objects.equals(this.apiVersion, v1SpringCloudGatewayMappingList.apiVersion) && Objects.equals(this.items, v1SpringCloudGatewayMappingList.items) && Objects.equals(this.kind, v1SpringCloudGatewayMappingList.kind) && Objects.equals(this.metadata, v1SpringCloudGatewayMappingList.metadata);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.apiVersion, this.items, this.kind, this.metadata});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayMappingList {\n");
        sb.append("    apiVersion: ").append(this.toIndentedString(this.apiVersion)).append("\n");
        sb.append("    items: ").append(this.toIndentedString(this.items)).append("\n");
        sb.append("    kind: ").append(this.toIndentedString(this.kind)).append("\n");
        sb.append("    metadata: ").append(this.toIndentedString(this.metadata)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
