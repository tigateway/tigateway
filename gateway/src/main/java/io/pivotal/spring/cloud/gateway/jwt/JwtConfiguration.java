package io.pivotal.spring.cloud.gateway.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class JwtConfiguration {
    public JwtConfiguration() {
    }

    @Bean
    public Clock jwtFileLocatorClock() {
        return Clock.systemUTC();
    }
}
