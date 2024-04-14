package ti.gateway.admin.core.sign;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import ti.gateway.admin.core.cache.AppServer;
import ti.gateway.admin.core.cache.AppServerCache;
import ti.gateway.admin.core.config.ApiGatewayAppProperties;
import ti.gateway.admin.core.enums.AppStatus;
import ti.gateway.util.ApiGatewaySignUtils;
import ti.gateway.util.MapUtils;
import ti.gateway.util.ServerHttpRequestUtils;

import java.util.Map;

/**
 * Default  impl {@link AccessAppSignCheck}
 */
public class DefaultAccessAppSignCheck implements AccessAppSignCheck {

    private static final Logger log = LoggerFactory.getLogger(DefaultAccessAppSignCheck.class);

    private ApiGatewayAppProperties apiGatewayAppProperties;

    private AppServerCache appServerCache;

    public DefaultAccessAppSignCheck(ApiGatewayAppProperties apiGatewayAppProperties, AppServerCache appServerCache) {
        this.apiGatewayAppProperties = apiGatewayAppProperties;
        this.appServerCache = appServerCache;
    }

    /**
     * check access
     *
     * @param request
     */
    @Override
    public void validAccessAppSign(ServerHttpRequest request) throws InvalidAccessTokenException {
        String appId = ServerHttpRequestUtils.getHttpHeaderParam(request, apiGatewayAppProperties.getAppId());
        if (StringUtils.isEmpty(appId)) {
            log.warn("appId not null");
            throw new InvalidAccessTokenException("appId not null");
        }
        String timeStamp = ServerHttpRequestUtils.getHttpHeaderParam(request, apiGatewayAppProperties.getTimestamp());
        if (StringUtils.isEmpty(appId)) {
            log.warn("timeStamp not null");
            throw new InvalidAccessTokenException("timeStamp not null");
        }
        String sign = ServerHttpRequestUtils.getHttpHeaderParam(request, apiGatewayAppProperties.getSign());
        if (StringUtils.isEmpty(sign)) {
            log.warn("sign not null");
            throw new InvalidAccessTokenException("sign not null");
        }
        Long durationMt = apiGatewayAppProperties.getDurationMt();
        long now = System.currentTimeMillis();
        long duration = Math.abs(now - Long.valueOf(timeStamp));
        if (duration > durationMt) {
            log.warn("[请求误差超过[{}]，被判定为非法伪造请求]", duration);
            throw new InvalidAccessTokenException("非法请求，请求时间戳已超时");
        }
        validAccessToken(appId, sign, timeStamp, request.getQueryParams());
    }

    /**
     * 校验APP服务
     *
     * @param appId
     * @param sign
     * @param queryParams
     */
    private void validAccessToken(String appId, String sign, String timeStamp,
                                  MultiValueMap<String, String> queryParams) throws InvalidAccessTokenException {
        AppServer appServer = appServerCache.get(appId);
        if (appServer == null) {
            log.warn("未识别的AppID[{}]", appId);
            throw new InvalidAccessTokenException("未识别的AppID [" + appId + "]");
        }
        Byte status = appServer.getStatus();
        switch (AppStatus.of(status)) {
            case OFF:
                throw new InvalidAccessTokenException("当前AppID已下线");
            default:
                break;
        }
        Map<String, String> regularMap = MapUtils.convertMultiToRegularMap(queryParams);
        regularMap.put(apiGatewayAppProperties.getAppId(), appId);
        regularMap.put(apiGatewayAppProperties.getTimestamp(), timeStamp);
        String originSign = ApiGatewaySignUtils.generateSignature(appServer.getAppsecret(), regularMap);
        if (!sign.equals(originSign)) {
            log.warn("Appid [{}] 两次Token不一致，原：[{}],目标：[{}]",
                    appId, sign, originSign);
            throw new InvalidAccessTokenException("非法请求，签名计算错误");
        }
    }

}
