package ti.gateway.core.server;

import org.springframework.web.server.ServerWebExchange;

/**
 * 校验当前应用下的服务授权
 */
public interface AppServerCheck {

    /**
     * check server
     *
     * @param exchange
     */
    void validAccessServer(ServerWebExchange exchange) throws InvalidAccessServerException;

}
