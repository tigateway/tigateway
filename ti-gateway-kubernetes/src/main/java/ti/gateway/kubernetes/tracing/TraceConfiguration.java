package ti.gateway.kubernetes.tracing;

import brave.http.HttpRequestParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
@ConditionalOnProperty({"spring.sleuth.enabled"})
public class TraceConfiguration {
    public TraceConfiguration() {
    }

    @Bean(
            name = {"sleuthHttpClientRequestParser", "sleuthHttpServerRequestParser"}
    )
    HttpRequestParser sleuthHttpServerRequestParser() {
        return (req, context, span) -> {
            HttpRequestParser.DEFAULT.parse(req, context, span);
            String method = req.method();
            String path = req.path();
            if (StringUtils.hasText(method) && StringUtils.hasText(path)) {
                span.name(String.format("%s %s", method, path));
            }

        };
    }
}
