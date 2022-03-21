package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecApiCorsPerRoute {
    public static final String SERIALIZED_NAME_ALLOW_CREDENTIALS = "allowCredentials";
    @SerializedName("allowCredentials")
    private Boolean allowCredentials;
    public static final String SERIALIZED_NAME_ALLOWED_HEADERS = "allowedHeaders";
    @SerializedName("allowedHeaders")
    private List<String> allowedHeaders = null;
    public static final String SERIALIZED_NAME_ALLOWED_METHODS = "allowedMethods";
    @SerializedName("allowedMethods")
    private List<String> allowedMethods = null;
    public static final String SERIALIZED_NAME_ALLOWED_ORIGIN_PATTERNS = "allowedOriginPatterns";
    @SerializedName("allowedOriginPatterns")
    private List<String> allowedOriginPatterns = null;
    public static final String SERIALIZED_NAME_ALLOWED_ORIGINS = "allowedOrigins";
    @SerializedName("allowedOrigins")
    private List<String> allowedOrigins = null;
    public static final String SERIALIZED_NAME_EXPOSED_HEADERS = "exposedHeaders";
    @SerializedName("exposedHeaders")
    private List<String> exposedHeaders = null;
    public static final String SERIALIZED_NAME_MAX_AGE = "maxAge";
    @SerializedName("maxAge")
    private Integer maxAge;

    public V1SpringCloudGatewaySpecApiCorsPerRoute() {
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute allowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
        return this;
    }

    @Nullable
    @ApiModelProperty("Whether user credentials are supported.")
    public Boolean getAllowCredentials() {
        return this.allowCredentials;
    }

    public void setAllowCredentials(Boolean allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute allowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
        return this;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute addAllowedHeadersItem(String allowedHeadersItem) {
        if (this.allowedHeaders == null) {
            this.allowedHeaders = new ArrayList();
        }

        this.allowedHeaders.add(allowedHeadersItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("Set the list of headers that a pre-flight request can list as allowed for use during an actual request. The special value \"*\" allows actual requests to send any header. A header name is not required to be listed if it is one of: Cache-Control, Content-Language, Expires, Last-Modified, or Pragma. ")
    public List<String> getAllowedHeaders() {
        return this.allowedHeaders;
    }

    public void setAllowedHeaders(List<String> allowedHeaders) {
        this.allowedHeaders = allowedHeaders;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute allowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
        return this;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute addAllowedMethodsItem(String allowedMethodsItem) {
        if (this.allowedMethods == null) {
            this.allowedMethods = new ArrayList();
        }

        this.allowedMethods.add(allowedMethodsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("Set the HTTP methods to allow, e.g. \"GET\", \"POST\", \"PUT\", etc. The special value \"*\" allows all methods. If not set, only \"GET\" and \"HEAD\" are allowed. ")
    public List<String> getAllowedMethods() {
        return this.allowedMethods;
    }

    public void setAllowedMethods(List<String> allowedMethods) {
        this.allowedMethods = allowedMethods;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute allowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
        return this;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute addAllowedOriginPatternsItem(String allowedOriginPatternsItem) {
        if (this.allowedOriginPatterns == null) {
            this.allowedOriginPatterns = new ArrayList();
        }

        this.allowedOriginPatterns.add(allowedOriginPatternsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("Alternative to allowedOrigins that supports more flexible origins patterns with \"*\" anywhere in the host name in addition to port lists. In contrast to allowedOrigins which only supports \"*\" and cannot be used with allowCredentials, when an allowedOriginPattern is matched, the Access-Control-Allow-Origin response header is set to the matched origin and not to \"*\" nor to the pattern. Therefore allowedOriginPatterns can be used in combination with allowCredentials set to true. ")
    public List<String> getAllowedOriginPatterns() {
        return this.allowedOriginPatterns;
    }

    public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
        this.allowedOriginPatterns = allowedOriginPatterns;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute allowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
        return this;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute addAllowedOriginsItem(String allowedOriginsItem) {
        if (this.allowedOrigins == null) {
            this.allowedOrigins = new ArrayList();
        }

        this.allowedOrigins.add(allowedOriginsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("Set the origins to allow, e.g. \"https://domain1.com\". The special value \"*\" allows all domains. ")
    public List<String> getAllowedOrigins() {
        return this.allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute exposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
        return this;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute addExposedHeadersItem(String exposedHeadersItem) {
        if (this.exposedHeaders == null) {
            this.exposedHeaders = new ArrayList();
        }

        this.exposedHeaders.add(exposedHeadersItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("Set the list of response headers other than simple headers (i.e. Cache-Control, Content-Language, Content-Type, Expires, Last-Modified, or Pragma) that an actual response might have and can be exposed. Note that \"*\" is not a valid exposed header value ")
    public List<String> getExposedHeaders() {
        return this.exposedHeaders;
    }

    public void setExposedHeaders(List<String> exposedHeaders) {
        this.exposedHeaders = exposedHeaders;
    }

    public V1SpringCloudGatewaySpecApiCorsPerRoute maxAge(Integer maxAge) {
        this.maxAge = maxAge;
        return this;
    }

    @Nullable
    @ApiModelProperty("Configure how long, in seconds, the response from a pre-flight request can be cached by clients.")
    public Integer getMaxAge() {
        return this.maxAge;
    }

    public void setMaxAge(Integer maxAge) {
        this.maxAge = maxAge;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecApiCorsPerRoute v1SpringCloudGatewaySpecApiCorsPerRoute = (V1SpringCloudGatewaySpecApiCorsPerRoute)o;
            return Objects.equals(this.allowCredentials, v1SpringCloudGatewaySpecApiCorsPerRoute.allowCredentials) && Objects.equals(this.allowedHeaders, v1SpringCloudGatewaySpecApiCorsPerRoute.allowedHeaders) && Objects.equals(this.allowedMethods, v1SpringCloudGatewaySpecApiCorsPerRoute.allowedMethods) && Objects.equals(this.allowedOriginPatterns, v1SpringCloudGatewaySpecApiCorsPerRoute.allowedOriginPatterns) && Objects.equals(this.allowedOrigins, v1SpringCloudGatewaySpecApiCorsPerRoute.allowedOrigins) && Objects.equals(this.exposedHeaders, v1SpringCloudGatewaySpecApiCorsPerRoute.exposedHeaders) && Objects.equals(this.maxAge, v1SpringCloudGatewaySpecApiCorsPerRoute.maxAge);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.allowCredentials, this.allowedHeaders, this.allowedMethods, this.allowedOriginPatterns, this.allowedOrigins, this.exposedHeaders, this.maxAge});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecApiCorsPerRoute {\n");
        sb.append("    allowCredentials: ").append(this.toIndentedString(this.allowCredentials)).append("\n");
        sb.append("    allowedHeaders: ").append(this.toIndentedString(this.allowedHeaders)).append("\n");
        sb.append("    allowedMethods: ").append(this.toIndentedString(this.allowedMethods)).append("\n");
        sb.append("    allowedOriginPatterns: ").append(this.toIndentedString(this.allowedOriginPatterns)).append("\n");
        sb.append("    allowedOrigins: ").append(this.toIndentedString(this.allowedOrigins)).append("\n");
        sb.append("    exposedHeaders: ").append(this.toIndentedString(this.exposedHeaders)).append("\n");
        sb.append("    maxAge: ").append(this.toIndentedString(this.maxAge)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
