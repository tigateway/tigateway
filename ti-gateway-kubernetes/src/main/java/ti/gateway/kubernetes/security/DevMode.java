package ti.gateway.kubernetes.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@ConditionalOnExpression("#{systemProperties['com.vmware.tanzu.springcloudgateway.dev.mode.enabled']!=null && systemProperties['com.vmware.tanzu.springcloudgateway.dev.mode.enabled'].equals('true')}")
public @interface DevMode {
}
