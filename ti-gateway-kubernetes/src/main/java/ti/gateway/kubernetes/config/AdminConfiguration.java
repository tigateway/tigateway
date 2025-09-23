package ti.gateway.kubernetes.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * Admin配置类
 * 配置Admin相关的路由和属性
 */
@Configuration
public class AdminConfiguration {

    @Bean
    @ConfigurationProperties("admin")
    public AdminProperties adminProperties() {
        return new AdminProperties();
    }

    /**
     * Admin属性配置
     */
    public static class AdminProperties {
        private Server server = new Server();
        private String contextPath = "/admin";

        public Server getServer() {
            return server;
        }

        public void setServer(Server server) {
            this.server = server;
        }

        public String getContextPath() {
            return contextPath;
        }

        public void setContextPath(String contextPath) {
            this.contextPath = contextPath;
        }

        public static class Server {
            private int port = 8081;

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }
        }
    }
}
