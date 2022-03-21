package io.pivotal.spring.cloud.gateway.jwt;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnProperty(
        value = {"jwtKey.enabled"},
        havingValue = "true"
)
public @interface JwtKeyEnabled {
}
