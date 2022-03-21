package io.pivotal.spring.cloud.gateway.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnProperty({"spring.security.oauth2.client.provider.sso.issuer-uri"})
public @interface SsoEnabled {
}
