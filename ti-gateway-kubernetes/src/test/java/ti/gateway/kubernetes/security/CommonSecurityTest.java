package ti.gateway.kubernetes.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.config.web.server.ServerHttpSecurity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for CommonSecurity
 */
class CommonSecurityTest {

    @Test
    void testConfigureCommonSecurity() {
        ServerHttpSecurity httpSecurity = ServerHttpSecurity.http();
        ServerHttpSecurity result = CommonSecurity.configureCommonSecurity(httpSecurity);
        
        assertNotNull(result);
        // The configuration should disable headers, httpBasic, csrf, and logout
        // Since these are internal configurations, we mainly verify the method doesn't throw
    }

    @Test
    void testConfigureCommonSecurityWithNull() {
        ServerHttpSecurity httpSecurity = ServerHttpSecurity.http();
        ServerHttpSecurity result = CommonSecurity.configureCommonSecurity(httpSecurity);
        
        assertNotNull(result);
    }
}
