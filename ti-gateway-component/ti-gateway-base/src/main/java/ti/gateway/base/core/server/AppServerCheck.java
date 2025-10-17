package ti.gateway.base.core.server;

import org.springframework.web.server.ServerWebExchange;

/**
 * 校验当前应用下的服务授权
 */
public interface AppServerCheck {

    /**
     * check server
     *
     * @param exchange ServerWebExchange
     */
    void validAccessServer(ServerWebExchange exchange) throws InvalidAccessServerException;

}
