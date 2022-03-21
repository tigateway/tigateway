package io.pivotal.spring.cloud.gateway.tls;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(
        prefix = "spring.cloud.gateway.k8s.tls"
)
class TlsConfiguration {
    public static final String PREFIX = "spring.cloud.gateway.k8s.tls";
    private List<TlsServer> servers;

    TlsConfiguration() {
    }

    public List<TlsServer> getServers() {
        return this.servers;
    }

    public TlsConfiguration setServers(List<TlsServer> servers) {
        this.servers = servers;
        return this;
    }

    public boolean isEnabled() {
        return this.servers != null && !this.servers.isEmpty();
    }

    public String toString() {
        return "TlsConfiguration{servers=" + this.servers + "}";
    }
}

