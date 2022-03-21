package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpec {
    public static final String SERIALIZED_NAME_API = "api";
    @SerializedName("api")
    private V1SpringCloudGatewaySpecApi api;
    public static final String SERIALIZED_NAME_COUNT = "count";
    @SerializedName("count")
    private Integer count;
    public static final String SERIALIZED_NAME_ENV = "env";
    @SerializedName("env")
    private List<V1SpringCloudGatewaySpecEnv> env = null;
    public static final String SERIALIZED_NAME_EXTENSIONS = "extensions";
    @SerializedName("extensions")
    private V1SpringCloudGatewaySpecExtensions extensions;
    public static final String SERIALIZED_NAME_JAVA_OPTS = "java-opts";
    @SerializedName("java-opts")
    private String javaOpts;
    public static final String SERIALIZED_NAME_OBSERVABILITY = "observability";
    @SerializedName("observability")
    private V1SpringCloudGatewaySpecObservability observability;
    public static final String SERIALIZED_NAME_RESOURCES = "resources";
    @SerializedName("resources")
    private V1SpringCloudGatewaySpecResources resources;
    public static final String SERIALIZED_NAME_SECURITY_CONTEXT = "securityContext";
    @SerializedName("securityContext")
    private V1SpringCloudGatewaySpecSecurityContext securityContext;
    public static final String SERIALIZED_NAME_SERVICE_ACCOUNT = "serviceAccount";
    @SerializedName("serviceAccount")
    private V1SpringCloudGatewaySpecServiceAccount serviceAccount;
    public static final String SERIALIZED_NAME_SSO = "sso";
    @SerializedName("sso")
    private V1SpringCloudGatewaySpecSso sso;
    public static final String SERIALIZED_NAME_TLS = "tls";
    @SerializedName("tls")
    private List<V1SpringCloudGatewaySpecTls> tls = null;

    public V1SpringCloudGatewaySpec() {
    }

    public V1SpringCloudGatewaySpec api(V1SpringCloudGatewaySpecApi api) {
        this.api = api;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecApi getApi() {
        return this.api;
    }

    public void setApi(V1SpringCloudGatewaySpecApi api) {
        this.api = api;
    }

    public V1SpringCloudGatewaySpec count(Integer count) {
        this.count = count;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public Integer getCount() {
        return this.count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public V1SpringCloudGatewaySpec env(List<V1SpringCloudGatewaySpecEnv> env) {
        this.env = env;
        return this;
    }

    public V1SpringCloudGatewaySpec addEnvItem(V1SpringCloudGatewaySpecEnv envItem) {
        if (this.env == null) {
            this.env = new ArrayList();
        }

        this.env.add(envItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public List<V1SpringCloudGatewaySpecEnv> getEnv() {
        return this.env;
    }

    public void setEnv(List<V1SpringCloudGatewaySpecEnv> env) {
        this.env = env;
    }

    public V1SpringCloudGatewaySpec extensions(V1SpringCloudGatewaySpecExtensions extensions) {
        this.extensions = extensions;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecExtensions getExtensions() {
        return this.extensions;
    }

    public void setExtensions(V1SpringCloudGatewaySpecExtensions extensions) {
        this.extensions = extensions;
    }

    public V1SpringCloudGatewaySpec javaOpts(String javaOpts) {
        this.javaOpts = javaOpts;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getJavaOpts() {
        return this.javaOpts;
    }

    public void setJavaOpts(String javaOpts) {
        this.javaOpts = javaOpts;
    }

    public V1SpringCloudGatewaySpec observability(V1SpringCloudGatewaySpecObservability observability) {
        this.observability = observability;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecObservability getObservability() {
        return this.observability;
    }

    public void setObservability(V1SpringCloudGatewaySpecObservability observability) {
        this.observability = observability;
    }

    public V1SpringCloudGatewaySpec resources(V1SpringCloudGatewaySpecResources resources) {
        this.resources = resources;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecResources getResources() {
        return this.resources;
    }

    public void setResources(V1SpringCloudGatewaySpecResources resources) {
        this.resources = resources;
    }

    public V1SpringCloudGatewaySpec securityContext(V1SpringCloudGatewaySpecSecurityContext securityContext) {
        this.securityContext = securityContext;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecSecurityContext getSecurityContext() {
        return this.securityContext;
    }

    public void setSecurityContext(V1SpringCloudGatewaySpecSecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    public V1SpringCloudGatewaySpec serviceAccount(V1SpringCloudGatewaySpecServiceAccount serviceAccount) {
        this.serviceAccount = serviceAccount;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecServiceAccount getServiceAccount() {
        return this.serviceAccount;
    }

    public void setServiceAccount(V1SpringCloudGatewaySpecServiceAccount serviceAccount) {
        this.serviceAccount = serviceAccount;
    }

    public V1SpringCloudGatewaySpec sso(V1SpringCloudGatewaySpecSso sso) {
        this.sso = sso;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public V1SpringCloudGatewaySpecSso getSso() {
        return this.sso;
    }

    public void setSso(V1SpringCloudGatewaySpecSso sso) {
        this.sso = sso;
    }

    public V1SpringCloudGatewaySpec tls(List<V1SpringCloudGatewaySpecTls> tls) {
        this.tls = tls;
        return this;
    }

    public V1SpringCloudGatewaySpec addTlsItem(V1SpringCloudGatewaySpecTls tlsItem) {
        if (this.tls == null) {
            this.tls = new ArrayList();
        }

        this.tls.add(tlsItem);
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public List<V1SpringCloudGatewaySpecTls> getTls() {
        return this.tls;
    }

    public void setTls(List<V1SpringCloudGatewaySpecTls> tls) {
        this.tls = tls;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpec v1SpringCloudGatewaySpec = (V1SpringCloudGatewaySpec)o;
            return Objects.equals(this.api, v1SpringCloudGatewaySpec.api) && Objects.equals(this.count, v1SpringCloudGatewaySpec.count) && Objects.equals(this.env, v1SpringCloudGatewaySpec.env) && Objects.equals(this.extensions, v1SpringCloudGatewaySpec.extensions) && Objects.equals(this.javaOpts, v1SpringCloudGatewaySpec.javaOpts) && Objects.equals(this.observability, v1SpringCloudGatewaySpec.observability) && Objects.equals(this.resources, v1SpringCloudGatewaySpec.resources) && Objects.equals(this.securityContext, v1SpringCloudGatewaySpec.securityContext) && Objects.equals(this.serviceAccount, v1SpringCloudGatewaySpec.serviceAccount) && Objects.equals(this.sso, v1SpringCloudGatewaySpec.sso) && Objects.equals(this.tls, v1SpringCloudGatewaySpec.tls);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.api, this.count, this.env, this.extensions, this.javaOpts, this.observability, this.resources, this.securityContext, this.serviceAccount, this.sso, this.tls});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpec {\n");
        sb.append("    api: ").append(this.toIndentedString(this.api)).append("\n");
        sb.append("    count: ").append(this.toIndentedString(this.count)).append("\n");
        sb.append("    env: ").append(this.toIndentedString(this.env)).append("\n");
        sb.append("    extensions: ").append(this.toIndentedString(this.extensions)).append("\n");
        sb.append("    javaOpts: ").append(this.toIndentedString(this.javaOpts)).append("\n");
        sb.append("    observability: ").append(this.toIndentedString(this.observability)).append("\n");
        sb.append("    resources: ").append(this.toIndentedString(this.resources)).append("\n");
        sb.append("    securityContext: ").append(this.toIndentedString(this.securityContext)).append("\n");
        sb.append("    serviceAccount: ").append(this.toIndentedString(this.serviceAccount)).append("\n");
        sb.append("    sso: ").append(this.toIndentedString(this.sso)).append("\n");
        sb.append("    tls: ").append(this.toIndentedString(this.tls)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}

