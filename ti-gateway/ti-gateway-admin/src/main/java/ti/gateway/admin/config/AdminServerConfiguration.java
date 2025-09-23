package ti.gateway.admin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
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
@EnableConfigurationProperties(AdminProperties.class)
public class AdminServerConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(AdminServerConfiguration.class);
    
    private DisposableServer adminServer;
    private final AdminProperties adminProperties;

    public AdminServerConfiguration(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    @Bean
    public RouterFunction<ServerResponse> adminRouterFunction() {
        return route()
                .GET("/admin/", request -> 
                    ServerResponse.ok().bodyValue("Hello, TiGateway Admin Server!"))
                .GET("/admin/health", request -> 
                    ServerResponse.ok().bodyValue("{\"status\":\"UP\",\"service\":\"tigateway-admin\"}"))
                .GET("/admin/info", request -> 
                    ServerResponse.ok().bodyValue("{\"name\":\"TiGateway Admin\",\"version\":\"1.0.0\",\"port\":" + adminProperties.getServer().getPort() + "}"))
                .build();
    }

    @Bean
    public ApplicationRunner adminServerRunner(RouterFunction<ServerResponse> adminRouterFunction) {
        return args -> {
            logger.info("AdminServerRunner started, checking Admin server configuration...");
            logger.info("Admin server enabled: {}, port: {}", adminProperties.getServer().isEnabled(), adminProperties.getServer().getPort());
            
            if (!adminProperties.getServer().isEnabled()) {
                logger.info("Admin server is disabled, skipping startup");
                return;
            }

            try {
                logger.info("Starting Admin server on port: {}", adminProperties.getServer().getPort());
                
                // 创建HttpHandler
                HttpHandler httpHandler = RouterFunctions.toHttpHandler(adminRouterFunction);
                logger.info("HttpHandler created successfully");

                // 创建ReactorHttpHandlerAdapter
                ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(httpHandler);
                logger.info("ReactorHttpHandlerAdapter created successfully");

                // 启动独立的HTTP服务器
                this.adminServer = HttpServer.create()
                        .port(adminProperties.getServer().getPort())
                        .handle(adapter)
                        .bindNow();

                logger.info("Admin server started successfully on port: {}", adminProperties.getServer().getPort());
            } catch (Exception e) {
                logger.error("Failed to start Admin server on port: {}", adminProperties.getServer().getPort(), e);
                // 不抛出异常，允许主应用继续运行
            }
        };
    }

    @PreDestroy
    public void shutdown() {
        if (adminServer != null) {
            adminServer.disposeNow();
            logger.info("Admin server stopped");
        }
    }
}
