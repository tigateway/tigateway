package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayRouteConfigSpecService {
    public static final String SERIALIZED_NAME_FILTERS = "filters";
    @SerializedName("filters")
    private List<String> filters = null;
    public static final String SERIALIZED_NAME_NAME = "name";
    @SerializedName("name")
    private String name;
    public static final String SERIALIZED_NAME_NAMESPACE = "namespace";
    @SerializedName("namespace")
    private String namespace;
    public static final String SERIALIZED_NAME_PORT = "port";
    @SerializedName("port")
    private Integer port;
    public static final String SERIALIZED_NAME_PREDICATES = "predicates";
    @SerializedName("predicates")
    private List<String> predicates = null;
    public static final String SERIALIZED_NAME_SSO_ENABLED = "ssoEnabled";
    @SerializedName("ssoEnabled")
    private Boolean ssoEnabled;

    public V1SpringCloudGatewayRouteConfigSpecService() {
    }

    public V1SpringCloudGatewayRouteConfigSpecService filters(List<String> filters) {
        this.filters = filters;
        return this;
    }

    public V1SpringCloudGatewayRouteConfigSpecService addFiltersItem(String filtersItem) {
        if (this.filters == null) {
            this.filters = new ArrayList();
        }

        this.filters.add(filtersItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public List<String> getFilters() {
        return this.filters;
    }

    public void setFilters(List<String> filters) {
        this.filters = filters;
    }

    public V1SpringCloudGatewayRouteConfigSpecService name(String name) {
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

    public V1SpringCloudGatewayRouteConfigSpecService namespace(String namespace) {
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

    public V1SpringCloudGatewayRouteConfigSpecService port(Integer port) {
        this.port = port;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public Integer getPort() {
        return this.port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public V1SpringCloudGatewayRouteConfigSpecService predicates(List<String> predicates) {
        this.predicates = predicates;
        return this;
    }

    public V1SpringCloudGatewayRouteConfigSpecService addPredicatesItem(String predicatesItem) {
        if (this.predicates == null) {
            this.predicates = new ArrayList();
        }

        this.predicates.add(predicatesItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public List<String> getPredicates() {
        return this.predicates;
    }

    public void setPredicates(List<String> predicates) {
        this.predicates = predicates;
    }

    public V1SpringCloudGatewayRouteConfigSpecService ssoEnabled(Boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public Boolean getSsoEnabled() {
        return this.ssoEnabled;
    }

    public void setSsoEnabled(Boolean ssoEnabled) {
        this.ssoEnabled = ssoEnabled;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayRouteConfigSpecService v1SpringCloudGatewayRouteConfigSpecService = (V1SpringCloudGatewayRouteConfigSpecService)o;
            return Objects.equals(this.filters, v1SpringCloudGatewayRouteConfigSpecService.filters) && Objects.equals(this.name, v1SpringCloudGatewayRouteConfigSpecService.name) && Objects.equals(this.namespace, v1SpringCloudGatewayRouteConfigSpecService.namespace) && Objects.equals(this.port, v1SpringCloudGatewayRouteConfigSpecService.port) && Objects.equals(this.predicates, v1SpringCloudGatewayRouteConfigSpecService.predicates) && Objects.equals(this.ssoEnabled, v1SpringCloudGatewayRouteConfigSpecService.ssoEnabled);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.filters, this.name, this.namespace, this.port, this.predicates, this.ssoEnabled});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayRouteConfigSpecService {\n");
        sb.append("    filters: ").append(this.toIndentedString(this.filters)).append("\n");
        sb.append("    name: ").append(this.toIndentedString(this.name)).append("\n");
        sb.append("    namespace: ").append(this.toIndentedString(this.namespace)).append("\n");
        sb.append("    port: ").append(this.toIndentedString(this.port)).append("\n");
        sb.append("    predicates: ").append(this.toIndentedString(this.predicates)).append("\n");
        sb.append("    ssoEnabled: ").append(this.toIndentedString(this.ssoEnabled)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
