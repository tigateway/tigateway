package ti.gateway.kubernetes.circuitbreaker;

import org.springframework.cloud.gateway.filter.factory.FallbackHeadersGatewayFilterFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ScgFallbackHeadersGatewayFilterFactory extends FallbackHeadersGatewayFilterFactory {
    public ScgFallbackHeadersGatewayFilterFactory() {
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(
                "executionExceptionTypeHeaderName",
                "executionExceptionMessageHeaderName",
                "rootCauseExceptionTypeHeaderName",
                "rootCauseExceptionMessageHeaderName"
        );
    }

    @Override
    public String name() {
        return "FallbackHeaders";
    }
}
