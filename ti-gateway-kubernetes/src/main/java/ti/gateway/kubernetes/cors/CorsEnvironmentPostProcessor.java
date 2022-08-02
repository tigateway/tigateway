package ti.gateway.kubernetes.cors;

import org.bouncycastle.util.encoders.Hex;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.logging.DeferredLog;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class CorsEnvironmentPostProcessor implements EnvironmentPostProcessor, InitializingBean {
    private static final DeferredLog looger = new DeferredLog();

    static final String SCG_K8S_GLOBAL_CONFIG_PREFIX = "spring.cloud.gateway.k8s.globalcors.";
    static final String SCG_K8S_PER_ROUTE_CONFIG_PREFIX = "spring.cloud.gateway.k8s.cors.";
    static final String SCG_OS_GLOBAL_PREFIX = "spring.cloud.gateway.globalcors.";
    static final String SCG_OS_CONFIG_PREFIX = "spring.cloud.gateway.globalcors.cors-configurations[/**]";

    private static final List<String> GLOBAL_CORS_OPTIONS = List.of(
            "spring.cloud.gateway.k8s.globalcors.allowed-origins",
            "spring.cloud.gateway.k8s.globalcors.allowed-methods",
            "spring.cloud.gateway.k8s.globalcors.allowed-headers",
            "spring.cloud.gateway.k8s.globalcors.allowe-credentials",
            "spring.cloud.gateway.k8s.globalcors.allowed-origin-patterns",
            "spring.cloud.gateway.k8s.globalcors.exposed-headers",
            "spring.cloud.gateway.k8s.globalcors.max-age"
    );

    static final String PROPERTY_SOURCE_NAME = CorsEnvironmentPostProcessor.class.getName();

    public CorsEnvironmentPostProcessor() {
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        looger.replayTo(this.getClass());
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Stream<String> corsOptionStream = GLOBAL_CORS_OPTIONS.stream();
        Objects.requireNonNull(environment);

        corsOptionStream = corsOptionStream
                .filter(environment::containsProperty)
                .filter(configOption -> {
                    return StringUtils.hasLength(environment.getProperty(configOption));
                });

        Function<String, String> stringFunction = (key) -> {
            return "spring.cloud.gateway.globalcors.cors-configurations[/**]." + key.substring("spring.cloud.gateway.k8s.globalcors.".length());
        };

        Objects.requireNonNull(environment);

        Map<String, Object> k8sCorsConfiguration = corsOptionStream.collect(Collectors.toMap(stringFunction, environment::getProperty));
        StreamSupport.stream(environment.getPropertySources().spliterator(), false)
                .filter(ps -> {
                    return ps instanceof EnumerablePropertySource;
                })
                .map(ps -> {
                    return ((EnumerablePropertySource<?>) ps).getPropertyNames();
                })
                .flatMap(Arrays::stream)
                .filter(propName -> {
                    return propName.startsWith("spring.cloud.gateway.k8s.cors.");
                })
                .filter(key -> {
                    return StringUtils.hasLength(environment.getProperty(key));
                })
                .forEach(key -> {
                    k8sCorsConfiguration.put(this.buildPropertyKey(key), environment.getProperty(key));
                });

        if (! k8sCorsConfiguration.isEmpty()) {
            k8sCorsConfiguration.put("spring.cloud.gateway.globalcors..add-to-simple-url-handler-mapping", true);
            DeferredLog deferredLog = looger;
            Stream<String> stream1 = k8sCorsConfiguration.keySet().stream();
            deferredLog.info("Applying CORS configuration: " + stream1.collect(Collectors.joining(", ")));
        }

        environment.getPropertySources().addLast(new MapPropertySource(PROPERTY_SOURCE_NAME, k8sCorsConfiguration));
    }

    private String buildPropertyKey(String key) {
        String[] encodedRouteAndHeader = key.substring("sping.cloud.gateway.k8s.cors.".length()).split("\\.");
        String encodedRoute = encodedRouteAndHeader[0];
        String corsHeader = encodedRouteAndHeader[1];
        String decodedRoute = new String(Hex.decode(encodedRoute));
        return String.format("%s%s%s.%s", "spring.cloud.gateway.globalcors.", "cors-configurations", decodedRoute, corsHeader);
    }
}
