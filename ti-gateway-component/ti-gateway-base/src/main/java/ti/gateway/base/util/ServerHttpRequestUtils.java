package ti.gateway.base.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.util.StringUtils;

public class ServerHttpRequestUtils {

    /**
     * get http Header
     *
     * @param serverHttpRequest ServerHttpRequest
     * @param key              key
     * @return value
     */
    public static String getHttpHeaderParam(ServerHttpRequest serverHttpRequest, String key) {
        HttpHeaders httpHeaders = serverHttpRequest.getHeaders();
        String param = httpHeaders.getFirst(key);
        if (StringUtils.isEmpty(param)) {
            param = serverHttpRequest.getQueryParams().getFirst(key);
        }
        return param;
    }

}
