package ti.gateway.admin.controller;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @version 1.0
 * @date 9/25/25 13:32
 */
@Component
public class AdminHandlerFunction implements HandlerFunction<ServerResponse> {
    @NotNull
    @Override
    public Mono<ServerResponse> handle(@NotNull ServerRequest request) {
        return ServerResponse.ok()
                .bodyValue("Hello, TiGateway Admin Server!");
    }
}
