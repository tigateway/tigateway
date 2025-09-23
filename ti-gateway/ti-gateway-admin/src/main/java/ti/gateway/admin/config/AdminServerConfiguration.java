package ti.gateway.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import javax.annotation.PreDestroy;


import static org.springframework.web.reactive.function.server.RouterFunctions.route;

/**
 * Admin服务器配置 - 提供独立端口访问
 */
@Configuration
@ConditionalOnProperty(
    name = "admin.server.enabled",
    havingValue = "true",
    matchIfMissing = true
)
@EnableConfigurationProperties(AdminServerProperties.class)
public class AdminServerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AdminServerConfiguration.class);
    
    private DisposableServer adminServer;
    private final AdminServerProperties adminServerProperties;

    public AdminServerConfiguration(AdminServerProperties adminServerProperties) {
        this.adminServerProperties = adminServerProperties;
    }

    @Bean
    public DisposableServer adminHttpServer() {
        try {
            // 创建简单的路由
            RouterFunction<ServerResponse> router = route()
                    .GET("/admin/", request -> 
                        ServerResponse.ok().bodyValue("Hello, TiGateway Admin Server!"))
                    .GET("/admin/health", request -> 
                        ServerResponse.ok().bodyValue("{\"status\":\"UP\",\"service\":\"tigateway-admin\"}"))
                    .GET("/admin/info", request -> 
                        ServerResponse.ok().bodyValue("{\"name\":\"TiGateway Admin\",\"version\":\"1.0.0\",\"port\":" + adminServerProperties.getPort() + "}"))
                    .build();

            // 创建HttpHandler
            HttpHandler httpHandler = RouterFunctions.toHttpHandler(router);

            // 创建ReactorHttpHandlerAdapter
            ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);

            // 启动独立的HTTP服务器
            this.adminServer = HttpServer.create()
                    .port(adminServerProperties.getPort())
                    .handle(adapter)
                    .bindNow();

            logger.info("Admin server started successfully on port: {}", adminServerProperties.getPort());
            return this.adminServer;
        } catch (Exception e) {
            logger.error("Failed to start Admin server on port: {}", adminServerProperties.getPort(), e);
            throw e;
        }
    }

    @PreDestroy
    public void shutdown() {
        if (adminServer != null) {
            adminServer.disposeNow();
            logger.info("Admin server stopped");
        }
    }
}
