package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewayRouteConfigSpecRoutes {
    public static final String SERIALIZED_NAME_DESCRIPTION = "description";
    @SerializedName("description")
    private String description;
    public static final String SERIALIZED_NAME_FILTERS = "filters";
    @SerializedName("filters")
    private List<String> filters = null;
    public static final String SERIALIZED_NAME_MODEL = "model";
    @SerializedName("model")
    private V1SpringCloudGatewayRouteConfigSpecModel model;
    public static final String SERIALIZED_NAME_ORDER = "order";
    @SerializedName("order")
    private Integer order;
    public static final String SERIALIZED_NAME_PREDICATES = "predicates";
    @SerializedName("predicates")
    private List<String> predicates = null;
    public static final String SERIALIZED_NAME_SSO_ENABLED = "ssoEnabled";
    @SerializedName("ssoEnabled")
    private Boolean ssoEnabled;
    public static final String SERIALIZED_NAME_TAGS = "tags";
    @SerializedName("tags")
    private List<String> tags = null;
    public static final String SERIALIZED_NAME_TITLE = "title";
    @SerializedName("title")
    private String title;
    public static final String SERIALIZED_NAME_TOKEN_RELAY = "tokenRelay";
    @SerializedName("tokenRelay")
    private Boolean tokenRelay;
    public static final String SERIALIZED_NAME_URI = "uri";
    @SerializedName("uri")
    private String uri;

    public V1SpringCloudGatewayRouteConfigSpecRoutes() {
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes description(String description) {
        this.description = description;
        return this;
    }

    @Nullable
    @ApiModelProperty("An optional description, intended to apply to all operations in this path.")
    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes filters(List<String> filters) {
        this.filters = filters;
        return this;
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes addFiltersItem(String filtersItem) {
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

    public V1SpringCloudGatewayRouteConfigSpecRoutes model(V1SpringCloudGatewayRouteConfigSpecModel model) {
        this.model = model;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewayRouteConfigSpecModel getModel() {
        return this.model;
    }

    public void setModel(V1SpringCloudGatewayRouteConfigSpecModel model) {
        this.model = model;
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes order(Integer order) {
        this.order = order;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public Integer getOrder() {
        return this.order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes predicates(List<String> predicates) {
        this.predicates = predicates;
        return this;
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes addPredicatesItem(String predicatesItem) {
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

    public V1SpringCloudGatewayRouteConfigSpecRoutes ssoEnabled(Boolean ssoEnabled) {
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

    public V1SpringCloudGatewayRouteConfigSpecRoutes tags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes addTagsItem(String tagsItem) {
        if (this.tags == null) {
            this.tags = new ArrayList();
        }

        this.tags.add(tagsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public List<String> getTags() {
        return this.tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes title(String title) {
        this.title = title;
        return this;
    }

    @Nullable
    @ApiModelProperty("An optional title, intended to apply to all operations in this path.")
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes tokenRelay(Boolean tokenRelay) {
        this.tokenRelay = tokenRelay;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public Boolean getTokenRelay() {
        return this.tokenRelay;
    }

    public void setTokenRelay(Boolean tokenRelay) {
        this.tokenRelay = tokenRelay;
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes uri(String uri) {
        this.uri = uri;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getUri() {
        return this.uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewayRouteConfigSpecRoutes v1SpringCloudGatewayRouteConfigSpecRoutes = (V1SpringCloudGatewayRouteConfigSpecRoutes)o;
            return Objects.equals(this.description, v1SpringCloudGatewayRouteConfigSpecRoutes.description) && Objects.equals(this.filters, v1SpringCloudGatewayRouteConfigSpecRoutes.filters) && Objects.equals(this.model, v1SpringCloudGatewayRouteConfigSpecRoutes.model) && Objects.equals(this.order, v1SpringCloudGatewayRouteConfigSpecRoutes.order) && Objects.equals(this.predicates, v1SpringCloudGatewayRouteConfigSpecRoutes.predicates) && Objects.equals(this.ssoEnabled, v1SpringCloudGatewayRouteConfigSpecRoutes.ssoEnabled) && Objects.equals(this.tags, v1SpringCloudGatewayRouteConfigSpecRoutes.tags) && Objects.equals(this.title, v1SpringCloudGatewayRouteConfigSpecRoutes.title) && Objects.equals(this.tokenRelay, v1SpringCloudGatewayRouteConfigSpecRoutes.tokenRelay) && Objects.equals(this.uri, v1SpringCloudGatewayRouteConfigSpecRoutes.uri);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.description, this.filters, this.model, this.order, this.predicates, this.ssoEnabled, this.tags, this.title, this.tokenRelay, this.uri});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewayRouteConfigSpecRoutes {\n");
        sb.append("    description: ").append(this.toIndentedString(this.description)).append("\n");
        sb.append("    filters: ").append(this.toIndentedString(this.filters)).append("\n");
        sb.append("    model: ").append(this.toIndentedString(this.model)).append("\n");
        sb.append("    order: ").append(this.toIndentedString(this.order)).append("\n");
        sb.append("    predicates: ").append(this.toIndentedString(this.predicates)).append("\n");
        sb.append("    ssoEnabled: ").append(this.toIndentedString(this.ssoEnabled)).append("\n");
        sb.append("    tags: ").append(this.toIndentedString(this.tags)).append("\n");
        sb.append("    title: ").append(this.toIndentedString(this.title)).append("\n");
        sb.append("    tokenRelay: ").append(this.toIndentedString(this.tokenRelay)).append("\n");
        sb.append("    uri: ").append(this.toIndentedString(this.uri)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

