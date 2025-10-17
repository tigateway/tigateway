package ti.gateway.base.core.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import ti.gateway.base.core.cache.AppServer;
import ti.gateway.base.core.cache.AppServerCache;
import ti.gateway.base.core.config.ApiGatewayAppProperties;
import ti.gateway.base.util.GatewayServerWebExchangeUtils;
import ti.gateway.base.util.ServerHttpRequestUtils;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Default impl {@link AppServerCheck}
 */
public class DefaultAppServerCheck implements AppServerCheck {

    private static final Logger log = LoggerFactory.getLogger(DefaultAppServerCheck.class);

    private final ApiGatewayAppProperties apiGatewayAppProperties;

    private final AppServerCache appServerCache;

    public DefaultAppServerCheck(ApiGatewayAppProperties apiGatewayAppProperties
            , AppServerCache appServerCache) {
        this.apiGatewayAppProperties = apiGatewayAppProperties;
        this.appServerCache = appServerCache;
    }

    /**
     * check server
     *
     * @param exchange ServerWebExchange
     */
    @Override
    public void validAccessServer(ServerWebExchange exchange) throws InvalidAccessServerException {
        ServerHttpRequest request = exchange.getRequest();
        String appId = ServerHttpRequestUtils.getHttpHeaderParam(request, apiGatewayAppProperties.getAppId());
        if (StringUtils.isEmpty(appId)) {
            log.warn("appId not null");
            throw new InvalidAccessServerException("appId not null");
        }
        AppServer appServer = appServerCache.get(appId);
        if (appServer == null) {
            log.warn("未识别的AppID[{}]", appId);
            throw new InvalidAccessServerException("未识别的AppID [" + appId + "]");
        }
        Set<AppServer.Server> servers = appServer.getServers();
        if (CollectionUtils.isEmpty(servers)) {
            log.warn("当前AppID[{}]未配置授权服务列表", appId);
            throw new InvalidAccessServerException("当前AppID[" + appId + "]未配置授权服务列表");
        } else {
            Object instance = exchange.getAttributes().get(GatewayServerWebExchangeUtils.GATEWAY_REQUEST_SERVICE_INSTANCE);
            if (instance == null) {
                String msg = "Unable to find instance for " + appId;
                throw new NotFoundException(msg);
            }
            List<String> serverCodes = servers.stream().map(AppServer.Server::getServerCode).collect(Collectors.toList());
            ServiceInstance serviceInstance = (ServiceInstance) instance;
            String serviceId = serviceInstance.getServiceId();
            if (!serverCodes.contains(serviceId)) {
                log.warn("当前AppID[{}]未授权[{}]服务", appId, serviceId);
                throw new InvalidAccessServerException("当前AppID[" + appId + "]未授权[" + serviceId + "]服务");
            }
        }

    }

}
