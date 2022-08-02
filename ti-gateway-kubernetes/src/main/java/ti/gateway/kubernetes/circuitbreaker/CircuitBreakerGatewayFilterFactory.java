package ti.gateway.kubernetes.circuitbreaker;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.core.IntervalFunction;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerFilterFactory;
import org.springframework.cloud.gateway.filter.factory.SpringCloudCircuitBreakerResilience4JFilterFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.DispatcherHandler;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CircuitBreakerGatewayFilterFactory extends AbstractGatewayFilterFactory<CircuitBreakerGatewayFilterFactory.Resilience4JExtendedConfig> {
    private final ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory;
    private final ObjectProvider<DispatcherHandler> dispatcherHandlerProvider;

    public CircuitBreakerGatewayFilterFactory(ReactiveCircuitBreakerFactory reactiveCircuitBreakerFactory, ObjectProvider<DispatcherHandler> dispatcherHandlerProvider) {
        this.reactiveCircuitBreakerFactory = reactiveCircuitBreakerFactory;
        this.dispatcherHandlerProvider = dispatcherHandlerProvider;
    }

    public List<String> shortcutFileOrder() {
        return Arrays.asList(
                "name",
                "fallbackUri",
                "statusCodes",
                "failureRateThreshold",
                "waitIntervalInOpenState"
        );
    }

    public Customizer<ReactiveResilience4JCircuitBreakerFactory> customizer(CircuitBreakerGatewayFilterFactory.Resilience4JExtendedConfig config) {
        return (factory) -> {
            factory.configure((builder) -> {
                CircuitBreakerConfig.Builder custom = CircuitBreakerConfig.custom();
                if (config.getFailureRateThreshold() != null) {
                    custom.failureRateThreshold(config.getFailureRateThreshold());
                }

                if (config.getWaitIntervalInOpenState() != null) {
                    custom.waitIntervalFunctionInOpenState(IntervalFunction.of(config.getWaitIntervalInOpenState()));
                }

                builder.circuitBreakerConfig(custom.build());
            }, new String[]{config.getId()});
        };
    }

    @Override
    public GatewayFilter apply(Resilience4JExtendedConfig config) {
        if (config.hasResilience4JCustomizations()) {
            this.customizer(config).customize((ReactiveResilience4JCircuitBreakerFactory) this.reactiveCircuitBreakerFactory);
        }

        SpringCloudCircuitBreakerResilience4JFilterFactory filterFactory = new SpringCloudCircuitBreakerResilience4JFilterFactory(this.reactiveCircuitBreakerFactory, this.dispatcherHandlerProvider);
        this.updateStatusCodes(config);

        return filterFactory.apply(config);
    }

    private void updateStatusCodes(Resilience4JExtendedConfig config) {
        if (this.hasShortcutStatusCodeConfiguration(config)) {
            config.setStatusCodes(this.splitShortcutStatusCode(config));
        }
    }

    private Set<String> splitShortcutStatusCode(Resilience4JExtendedConfig config) {
        return config.getStatusCodes().stream().flatMap((s) -> {
            return Arrays.stream(s.split(":"));
        }).collect(Collectors.toSet());
    }

    private boolean hasShortcutStatusCodeConfiguration(Resilience4JExtendedConfig config) {
        return config.getStatusCodes().stream().anyMatch((s) -> {
            return s.contains(":");
        });
    }

    @Override
    public Class<Resilience4JExtendedConfig> getConfigClass() {
        return Resilience4JExtendedConfig.class;
    }

    static class Resilience4JExtendedConfig extends SpringCloudCircuitBreakerFilterFactory.Config {
        private Float failureRateThreshold;
        private Duration waitIntervalInOpenState;

        public Resilience4JExtendedConfig() {
        }

        public Float getFailureRateThreshold() {
            return failureRateThreshold;
        }

        public void setFailureRateThreshold(Float failureRateThreshold) {
            this.failureRateThreshold = failureRateThreshold;
        }

        public Duration getWaitIntervalInOpenState() {
            return waitIntervalInOpenState;
        }

        public void setWaitIntervalInOpenState(Duration waitIntervalInOpenState) {
            this.waitIntervalInOpenState = waitIntervalInOpenState;
        }

        public boolean hasResilience4JCustomizations() {
            return this.failureRateThreshold != null || this.waitIntervalInOpenState != null;
        }
    }

}
