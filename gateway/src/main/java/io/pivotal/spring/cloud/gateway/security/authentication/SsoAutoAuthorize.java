package io.pivotal.spring.cloud.gateway.security.authentication;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnProperty(
        value = {"com.vmware.tanzu.springcloudgateway.sso.auto.authorize.enabled"},
        havingValue = "true"
)
public @interface SsoAutoAuthorize {
}
