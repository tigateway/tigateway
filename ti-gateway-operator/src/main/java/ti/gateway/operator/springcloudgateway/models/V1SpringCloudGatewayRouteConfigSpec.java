package ti.gateway.operator.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayRouteConfigSpec {
    public static final String SERIALIZED_NAME_BASIC_AUTH = "basicAuth";
    @SerializedName("basicAuth")
    private V1SpringCloudGatewayRouteConfigSpecBasicAuth basicAuth;
    public static final String SERIALIZED_NAME_OPENAPI = "openapi";
    @SerializedName("openapi")
    private V1SpringCloudGatewayRouteConfigSpecOpenapi openapi;
    public static final String SERIALIZED_NAME_ROUTES = "routes";
    @SerializedName("routes")
    private List<V1SpringCloudGatewayRouteConfigSpecRoutes> routes = null;
    public static final String SERIALIZED_NAME_SERVICE = "service";
    @SerializedName("service")
    private V1SpringCloudGatewayRouteConfigSpecService service;

    public V1SpringCloudGatewayRouteConfigSpec() {
    }

    public V1SpringCloudGatewayRouteConfigSpec basicAuth(V1SpringCloudGatewayRouteConfigSpecBasicAuth basicAuth) {
        this.basicAuth = basicAuth;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewayRouteConfigSpecBasicAuth getBasicAuth() {
        return this.basicAuth;
    }

    public void setBasicAuth(V1SpringCloudGatewayRouteConfigSpecBasicAuth basicAuth) {
        this.basicAuth = basicAuth;
    }

    public V1SpringCloudGatewayRouteConfigSpec openapi(V1SpringCloudGatewayRouteConfigSpecOpenapi openapi) {
        this.openapi = openapi;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewayRouteConfigSpecOpenapi getOpenapi() {
        return this.openapi;
    }

    public void setOpenapi(V1SpringCloudGatewayRouteConfigSpecOpenapi openapi) {
        this.openapi = openapi;
    }

    public V1SpringCloudGatewayRouteConfigSpec routes(List<V1SpringCloudGatewayRouteConfigSpecRoutes> routes) {
        this.routes = routes;
        return this;
    }

    public V1SpringCloudGatewayRouteConfigSpec addRoutesItem(V1SpringCloudGatewayRouteConfigSpecRoutes routesItem) {
        if (this.routes == null) {
            this.routes = new ArrayList();
        }

        this.routes.add(routesItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public List<V1SpringCloudGatewayRouteConfigSpecRoutes> getRoutes() {
        return this.routes;
    }

    public void setRoutes(List<V1SpringCloudGatewayRouteConfigSpecRoutes> routes) {
        this.routes = routes;
    }

    public V1SpringCloudGatewayRouteConfigSpec service(V1SpringCloudGatewayRouteConfigSpecService service) {
        this.service = service;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewayRouteConfigSpecService getService() {
        return this.service;
    }

    public void setService(V1SpringCloudGatewayRouteConfigSpecService service) {
        this.service = service;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayRouteConfigSpec v1SpringCloudGatewayRouteConfigSpec = (V1SpringCloudGatewayRouteConfigSpec)o;
            return Objects.equals(this.basicAuth, v1SpringCloudGatewayRouteConfigSpec.basicAuth) && Objects.equals(this.openapi, v1SpringCloudGatewayRouteConfigSpec.openapi) && Objects.equals(this.routes, v1SpringCloudGatewayRouteConfigSpec.routes) && Objects.equals(this.service, v1SpringCloudGatewayRouteConfigSpec.service);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.basicAuth, this.openapi, this.routes, this.service});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayRouteConfigSpec {\n");
        sb.append("    basicAuth: ").append(this.toIndentedString(this.basicAuth)).append("\n");
        sb.append("    openapi: ").append(this.toIndentedString(this.openapi)).append("\n");
        sb.append("    routes: ").append(this.toIndentedString(this.routes)).append("\n");
        sb.append("    service: ").append(this.toIndentedString(this.service)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
