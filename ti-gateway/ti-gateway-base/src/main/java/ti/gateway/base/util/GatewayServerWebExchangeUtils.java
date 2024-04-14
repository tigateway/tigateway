package ti.gateway.base.util;

public class GatewayServerWebExchangeUtils {

    public static final String GATEWAY_REQUEST_SERVICE_INSTANCE = qualify("requestServiceInstance");

    private static String qualify(String attr) {
        return GatewayServerWebExchangeUtils.class.getName() + "." + attr;
    }

}
