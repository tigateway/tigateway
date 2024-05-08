package ti.gateway.base.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.*;

/**
 * API 网关签名工具类
 */
public class ApiGatewaySignUtils {

    /**
     * 生成签名
     *
     * @param appsecret  应用密钥
     * @param queryParams 请求参数
     * @return 签名
     */
    public static String generateSignature(String appsecret, Map<String, String> queryParams) {
        String origin = origin(queryParams);
        return DigestUtils.md5Hex(origin + appsecret);
    }

    /**
     * 获取原始排序值
     *
     * @param queryParams 请求参数
     * @return 原始排序值
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
