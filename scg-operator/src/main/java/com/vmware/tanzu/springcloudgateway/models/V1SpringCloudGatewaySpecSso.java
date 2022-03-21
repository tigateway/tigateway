package com.vmware.tanzu.springcloudgateway.models;

import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;
import javax.annotation.Nullable;

public class V1SpringCloudGatewaySpecSso {
    public static final String SERIALIZED_NAME_INACTIVE_SESSION_EXPIRATION_IN_MINUTES = "inactive-session-expiration-in-minutes";
    @SerializedName("inactive-session-expiration-in-minutes")
    private Integer inactiveSessionExpirationInMinutes;
    public static final String SERIALIZED_NAME_ROLES_ATTRIBUTE_NAME = "roles-attribute-name";
    @SerializedName("roles-attribute-name")
    private String rolesAttributeName;
    public static final String SERIALIZED_NAME_SECRET = "secret";
    @SerializedName("secret")
    private String secret;

    public V1SpringCloudGatewaySpecSso() {
    }

    public V1SpringCloudGatewaySpecSso inactiveSessionExpirationInMinutes(Integer inactiveSessionExpirationInMinutes) {
        this.inactiveSessionExpirationInMinutes = inactiveSessionExpirationInMinutes;
        return this;
    }

    @Nullable
    @ApiModelProperty("Time to life of inactive sessions, 0 means infinite (in minutes).")
    public Integer getInactiveSessionExpirationInMinutes() {
        return this.inactiveSessionExpirationInMinutes;
    }

    public void setInactiveSessionExpirationInMinutes(Integer inactiveSessionExpirationInMinutes) {
        this.inactiveSessionExpirationInMinutes = inactiveSessionExpirationInMinutes;
    }

    public V1SpringCloudGatewaySpecSso rolesAttributeName(String rolesAttributeName) {
        this.rolesAttributeName = rolesAttributeName;
        return this;
    }

    @Nullable
    @ApiModelProperty("")
    public String getRolesAttributeName() {
        return this.rolesAttributeName;
    }

    public void setRolesAttributeName(String rolesAttributeName) {
        this.rolesAttributeName = rolesAttributeName;
    }

    public V1SpringCloudGatewaySpecSso secret(String secret) {
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

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            V1SpringCloudGatewaySpecSso v1SpringCloudGatewaySpecSso = (V1SpringCloudGatewaySpecSso)o;
            return Objects.equals(this.inactiveSessionExpirationInMinutes, v1SpringCloudGatewaySpecSso.inactiveSessionExpirationInMinutes) && Objects.equals(this.rolesAttributeName, v1SpringCloudGatewaySpecSso.rolesAttributeName) && Objects.equals(this.secret, v1SpringCloudGatewaySpecSso.secret);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.inactiveSessionExpirationInMinutes, this.rolesAttributeName, this.secret});
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class V1SpringCloudGatewaySpecSso {\n");
        sb.append("    inactiveSessionExpirationInMinutes: ").append(this.toIndentedString(this.inactiveSessionExpirationInMinutes)).append("\n");
        sb.append("    rolesAttributeName: ").append(this.toIndentedString(this.rolesAttributeName)).append("\n");
        sb.append("    secret: ").append(this.toIndentedString(this.secret)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    private String toIndentedString(Object o) {
        return o == null ? "null" : o.toString().replace("\n", "\n    ");
    }
}
