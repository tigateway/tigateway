package ti.gateway.kubernetes.security;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.SecureHeadersProperties;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@ConditionalOnProperty(
        value = {"spring.cloud.gateway.secure-headers.disabled"},
        matchIfMissing = true,
        havingValue = "false"
)
public class SecureHeadersGatewayGlobalFilter implements GlobalFilter, Ordered {
    private static final String DEFAULT_CACHE_CONTROL_VALUE = "no-cache, no-store, max-age=0, must-revalidate";
    private static final String DEFAULT_PRAGMA_VALUE = "no-cache";
    private static final String DEFAULT_EXPIRES_VALUE = "0";
    private static final Map<String, String> DEFAULT_SECURE_HEADERS = new HashMap<>();
    private final SecureHeadersProperties properties;

    public SecureHeadersGatewayGlobalFilter(SecureHeadersProperties properties) {
        this.properties = properties;
        DEFAULT_SECURE_HEADERS.put("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate");
        DEFAULT_SECURE_HEADERS.put("Pragma", "no-cache");
        DEFAULT_SECURE_HEADERS.put("Expires", "0");
        DEFAULT_SECURE_HEADERS.put("X-Xss-Protection", properties.getXssProtectionHeader());
        DEFAULT_SECURE_HEADERS.put("Strict-Transport-Security", properties.getStrictTransportSecurity());
        DEFAULT_SECURE_HEADERS.put("X-Frame-Options", properties.getFrameOptions());
        DEFAULT_SECURE_HEADERS.put("X-Content-Type-Options", properties.getContentTypeOptions());
    }

    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpHeaders headers = exchange.getResponse().getHeaders();
            List<String> disabled = this.properties.getDisable();
            DEFAULT_SECURE_HEADERS.forEach((header, value) -> {
                if (!disabled.contains(header.toLowerCase()) && !headers.containsKey(header)) {
                    headers.add(header, value);
                }
            });
        }));
    }

    public int getOrder() {
        return 2147483647;
    }
}
