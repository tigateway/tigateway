package ti.gateway.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

/**
 * API 网关签名工具类
 */
public class ApiGatewaySignUtils {

    /**
     * 生成签名
     *
     * @param appsecret
     * @param queryParams
     * @return
     */
    public static String generateSignature(String appsecret, Map<String, String> queryParams) {
        String origin = origin(queryParams);
        return DigestUtils.md5Hex(origin + appsecret);
    }

    /**
     * 获取原始排序值
     *
     * @param queryParams
     * @return
     */
    private static String origin(Map<String, String> queryParams) {
        StringBuilder stringBuilder = new StringBuilder();
        Set<String> keySet = queryParams.keySet();
        List<String> keys = new ArrayList<>(keySet);
        Collections.sort(keys);
        for (String key : keys) {
            stringBuilder.append(key).append(queryParams.get(key));
        }
        return stringBuilder.toString();
    }

}
