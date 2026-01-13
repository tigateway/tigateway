package ti.gateway.kubernetes.security;

import org.springframework.security.config.web.server.ServerHttpSecurity;

/**
 * Common Security Configuration
 * 
 * Configures common security settings for TiGateway using Spring Security 6.1+ API.
 */
public class CommonSecurity {
    public CommonSecurity() {
    }

    public static ServerHttpSecurity configureCommonSecurity(ServerHttpSecurity httpSecurity) {
        return httpSecurity
                .headers(headers -> headers.disable())
                .httpBasic(httpBasic -> httpBasic.disable())
                .csrf(csrf -> csrf.disable())
                .logout(logout -> logout.disable());
    }
}
