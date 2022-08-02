package ti.gateway.kubernetes.cors;

import org.springframework.web.cors.CorsConfiguration;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class CorsGatewayFilterConfig {
    private final CorsConfiguration corsConfiguration = new CorsConfiguration();

    public CorsGatewayFilterConfig() {
    }

    public CorsConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCors(String cors) {
        String parsedCors = cors.replace("[", "").replace("]", "");
        List<String> tokens = List.of(parsedCors.split(","));
        Iterator<String> iterator = tokens.iterator();

        while (iterator.hasNext()) {
            String token = (String) iterator.next();

            this.findValue(token, "allowCredentials:").ifPresent((value) -> {
                this.corsConfiguration.setAllowCredentials(Boolean.valueOf((String) value));
            });

            this.findValue(token, "allowedHeaders:").ifPresent((value) -> {
                this.corsConfiguration.setAllowedHeaders(this.parseList((String) value));
            });

            this.findValue(token, "allowedMethods:").ifPresent((value) -> {
                this.corsConfiguration.setAllowedMethods(this.parseList((String) value));
            });

            this.findValue(token, "allowedOriginPatterns:").ifPresent((value) -> {
                this.corsConfiguration.setAllowedOriginPatterns(this.parseList((String) value));
            });

            this.findValue(token, "allowedOrigins:").ifPresent((value) -> {
                this.corsConfiguration.setAllowedOrigins(this.parseList((String) value));
            });

            this.findValue(token, "exposedHeaders:").ifPresent((value) -> {
                this.corsConfiguration.setExposedHeaders(this.parseList((String) value));
            });

            this.findValue(token, "maxAge:").ifPresent((value) -> {
                this.corsConfiguration.setMaxAge(Long.valueOf((String) value));
            });

        }
    }

    private Optional<Object> findValue(String token, String key) {
        String value = token.startsWith(key) ? token.substring(key.length()) : null;
        return Optional.ofNullable(value);
    }

    private List<String> parseList(String value) {
        return List.of(value.split(";"));
    }
}
