package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecApi {
    public static final String SERIALIZED_NAME_CORS = "cors";
    @SerializedName("cors")
    private V1SpringCloudGatewaySpecApiCors cors;
    public static final String SERIALIZED_NAME_DESCRIPTION = "description";
    @SerializedName("description")
    private String description;
    public static final String SERIALIZED_NAME_DOCUMENTATION = "documentation";
    @SerializedName("documentation")
    private String documentation;
    public static final String SERIALIZED_NAME_GROUP_ID = "groupId";
    @SerializedName("groupId")
    private String groupId;
    public static final String SERIALIZED_NAME_SERVER_URL = "serverUrl";
    @SerializedName("serverUrl")
    private String serverUrl;
    public static final String SERIALIZED_NAME_TITLE = "title";
    @SerializedName("title")
    private String title;
    public static final String SERIALIZED_NAME_VERSION = "version";
    @SerializedName("version")
    private String version;

    public V1SpringCloudGatewaySpecApi() {
    }

    public V1SpringCloudGatewaySpecApi cors(V1SpringCloudGatewaySpecApiCors cors) {
        this.cors = cors;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecApiCors getCors() {
        return this.cors;
    }

    public void setCors(V1SpringCloudGatewaySpecApiCors cors) {
        this.cors = cors;
    }

    public V1SpringCloudGatewaySpecApi description(String description) {
        this.description = description;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public V1SpringCloudGatewaySpecApi documentation(String documentation) {
        this.documentation = documentation;
        return this;
    }

    @Nullable
    @ApiModelProperty("The URL of an external resource for extended documentation. Value MUST be in the format of a URL.")
    public String getDocumentation() {
        return this.documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public V1SpringCloudGatewaySpecApi groupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getGroupId() {
        return this.groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public V1SpringCloudGatewaySpecApi serverUrl(String serverUrl) {
        this.serverUrl = serverUrl;
        return this;
    }

    @Nullable
    @ApiModelProperty("Publicly accessible user-facing URL of this Gateway instance.")
    public String getServerUrl() {
        return this.serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public V1SpringCloudGatewaySpecApi title(String title) {
        this.title = title;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public V1SpringCloudGatewaySpecApi version(String version) {
        this.version = version;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecApi v1SpringCloudGatewaySpecApi = (V1SpringCloudGatewaySpecApi)o;
            return Objects.equals(this.cors, v1SpringCloudGatewaySpecApi.cors) && Objects.equals(this.description, v1SpringCloudGatewaySpecApi.description) && Objects.equals(this.documentation, v1SpringCloudGatewaySpecApi.documentation) && Objects.equals(this.groupId, v1SpringCloudGatewaySpecApi.groupId) && Objects.equals(this.serverUrl, v1SpringCloudGatewaySpecApi.serverUrl) && Objects.equals(this.title, v1SpringCloudGatewaySpecApi.title) && Objects.equals(this.version, v1SpringCloudGatewaySpecApi.version);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.cors, this.description, this.documentation, this.groupId, this.serverUrl, this.title, this.version});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecApi {\n");
        sb.append("    cors: ").append(this.toIndentedString(this.cors)).append("\n");
        sb.append("    description: ").append(this.toIndentedString(this.description)).append("\n");
        sb.append("    documentation: ").append(this.toIndentedString(this.documentation)).append("\n");
        sb.append("    groupId: ").append(this.toIndentedString(this.groupId)).append("\n");
        sb.append("    serverUrl: ").append(this.toIndentedString(this.serverUrl)).append("\n");
        sb.append("    title: ").append(this.toIndentedString(this.title)).append("\n");
        sb.append("    version: ").append(this.toIndentedString(this.version)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

