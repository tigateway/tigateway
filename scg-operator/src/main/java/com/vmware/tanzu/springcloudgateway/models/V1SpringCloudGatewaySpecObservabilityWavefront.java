package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecObservabilityWavefront {
    public static final String SERIALIZED_NAME_APPLICATION = "application";
    @SerializedName("application")
    private String application;
    public static final String SERIALIZED_NAME_SECRET = "secret";
    @SerializedName("secret")
    private String secret;
    public static final String SERIALIZED_NAME_SERVICE = "service";
    @SerializedName("service")
    private String service;
    public static final String SERIALIZED_NAME_SOURCE = "source";
    @SerializedName("source")
    private String source;

    public V1SpringCloudGatewaySpecObservabilityWavefront() {
    }

    public V1SpringCloudGatewaySpecObservabilityWavefront application(String application) {
        this.application = application;
        return this;
    }

    @Nullable
    @ApiModelProperty("The wavefront application name. ")
    public String getApplication() {
        return this.application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public V1SpringCloudGatewaySpecObservabilityWavefront secret(String secret) {
        this.secret = secret;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getSecret() {
        return this.secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public V1SpringCloudGatewaySpecObservabilityWavefront service(String service) {
        this.service = service;
        return this;
    }

    @Nullable
    @ApiModelProperty("The wavefront service name. ")
    public String getService() {
        return this.service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public V1SpringCloudGatewaySpecObservabilityWavefront source(String source) {
        this.source = source;
        return this;
    }

    @Nullable
    @ApiModelProperty("The wavefront source. ")
    public String getSource() {
        return this.source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecObservabilityWavefront v1SpringCloudGatewaySpecObservabilityWavefront = (V1SpringCloudGatewaySpecObservabilityWavefront)o;
            return Objects.equals(this.application, v1SpringCloudGatewaySpecObservabilityWavefront.application) && Objects.equals(this.secret, v1SpringCloudGatewaySpecObservabilityWavefront.secret) && Objects.equals(this.service, v1SpringCloudGatewaySpecObservabilityWavefront.service) && Objects.equals(this.source, v1SpringCloudGatewaySpecObservabilityWavefront.source);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.application, this.secret, this.service, this.source});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecObservabilityWavefront {\n");
        sb.append("    application: ").append(this.toIndentedString(this.application)).append("\n");
        sb.append("    secret: ").append(this.toIndentedString(this.secret)).append("\n");
        sb.append("    service: ").append(this.toIndentedString(this.service)).append("\n");
        sb.append("    source: ").append(this.toIndentedString(this.source)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

