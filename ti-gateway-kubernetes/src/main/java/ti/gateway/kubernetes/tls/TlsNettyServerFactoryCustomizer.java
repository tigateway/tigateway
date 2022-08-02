package ti.gateway.kubernetes.tls;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.NettyServerCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.server.HttpServer;
import reactor.netty.tcp.SslProvider.SslContextSpec;

@Component
@EnableConfigurationProperties({TlsConfiguration.class})
@ConditionalOnTlsEnabled
class TlsNettyServerFactoryCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {
    private static final Logger LOG = LoggerFactory.getLogger(TlsNettyServerFactoryCustomizer.class);
    private final TlsConfiguration tlsConfiguration;

    private TlsNettyServerFactoryCustomizer(TlsConfiguration tlsConfiguration) {
        this.tlsConfiguration = tlsConfiguration;
    }

    public void customize(NettyReactiveWebServerFactory serverFactory) {
        if (this.tlsConfiguration.isEnabled()) {
            serverFactory.addServerCustomizers(new NettyServerCustomizer[]{this::tlsCustomizer});
        }
    }

    HttpServer tlsCustomizer(HttpServer httpServer) {
        return httpServer.port(8443).secure(this::sniSslContext);
    }

    void sniSslContext(SslContextSpec sslContextSpec) {
        try {
            SelfSignedCertificate defaultCert = new SelfSignedCertificate("Spring Cloud Gateway Fake Certificate");
            SslContext defaultSslContext = Http11SslContextSpec.forServer(defaultCert.key(), new X509Certificate[]{defaultCert.cert()}).sslContext();
            sslContextSpec.sslContext(defaultSslContext).addSniMappings(this.buildSniMappings());
        } catch (Exception var4) {
            throw new RuntimeException("Unable to configure Netty SNI", var4);
        }
    }

    Map<String, Consumer<? super SslContextSpec>> buildSniMappings() {
        HashMap<String, Consumer<? super SslContextSpec>> sniMappings = new HashMap<>();
        Iterator<TlsServer> iterator = this.tlsConfiguration.getServers().iterator();

        while (iterator.hasNext()) {
            TlsServer server = (TlsServer) iterator.next();
            LOG.info("Requests to {} will use TLS context configured using {}", server.getHosts(), server.getSecret());
            Consumer<? super SslContextSpec> sslContextBuilder = this.sslContextBuilderFor(server);
            server.getHosts().forEach((host) -> {
                sniMappings.put(host, sslContextBuilder);
            });
        }

        return sniMappings;
    }

    Consumer<? super SslContextSpec> sslContextBuilderFor(TlsServer server) {
        return (sslContextSpec) -> {
            sslContextSpec.sslContext(Http11SslContextSpec.forServer(server.getSecret().getCertFile(), server.getSecret().getKeyFile()));
        };
    }
}

