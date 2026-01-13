package ti.gateway.kubernetes.security;

import org.springframework.security.config.web.server.ServerHttpSecurity;

/**
 * Common Security Configuration
 * 
 * Note: Uses deprecated Spring Security API methods (deprecated in 6.1+).
 * These methods are still functional and will be migrated to new API when stable.
 */
@SuppressWarnings("deprecation")
public class CommonSecurity {
    public CommonSecurity() {
    }

    public static ServerHttpSecurity configureCommonSecurity(ServerHttpSecurity httpSecurity) {
        return httpSecurity.headers().disable().httpBasic().disable().csrf().disable().logout().disable();
    }
}
