package ti.gateway.base.core.sign;

import org.springframework.http.server.reactive.ServerHttpRequest;

/**
 * check sign
 */
public interface AccessAppSignCheck {

    /**
     * check access
     *
     * @param request
     */
    void validAccessAppSign(ServerHttpRequest request) throws InvalidAccessTokenException;

}
