package ti.gateway.admin.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import ti.gateway.admin.config.AdminProperties;

/**
 * @version 1.0
 * @date 9/25/25 13:34
 */
@Component
public class AdminInfoHandlerFunction implements HandlerFunction<ServerResponse> {

    private final AdminProperties adminProperties;

    @Autowired
    public AdminInfoHandlerFunction(AdminProperties adminProperties) {
        this.adminProperties = adminProperties;
    }

    @Override
    @NotNull
    public Mono<ServerResponse> handle(@NotNull ServerRequest request) {
        return ServerResponse.ok()
                .bodyValue("{\"name\":\"TiGateway Admin\",\"version\":\"1.0.0\",\"port\":" + adminProperties.getServer().getPort() + "}");
    }
}
