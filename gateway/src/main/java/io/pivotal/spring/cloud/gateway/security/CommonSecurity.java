package io.pivotal.spring.cloud.gateway.security;

import org.springframework.security.config.web.server.ServerHttpSecurity;

public class CommonSecurity {
    public CommonSecurity() {
    }

    public static ServerHttpSecurity configureCommonSecurity(ServerHttpSecurity httpSecurity) {
        return httpSecurity.headers().disable().httpBasic().disable().csrf().disable().logout().disable();
    }
}
