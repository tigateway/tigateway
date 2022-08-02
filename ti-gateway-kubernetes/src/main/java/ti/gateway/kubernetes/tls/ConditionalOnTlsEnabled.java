package ti.gateway.kubernetes.tls;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.autoconfigure.condition.AllNestedConditions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Conditional;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional({ConditionalOnTlsEnabled.OnTlsEnabled.class})
@interface ConditionalOnTlsEnabled {
    public static class OnTlsEnabled extends AllNestedConditions {
        public OnTlsEnabled() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnProperty(
                prefix = "spring.cloud.gateway.k8s.tls",
                name = {"servers[0].secret"}
        )
        static class HasAConfiguredSecret {
            HasAConfiguredSecret() {
            }
        }

        @ConditionalOnProperty(
                prefix = "spring.cloud.gateway.k8s.tls",
                name = {"servers[0].hosts[0]"}
        )
        static class HasAtLeastOneTlsEnabledHost {
            HasAtLeastOneTlsEnabledHost() {
            }
        }
    }
}

