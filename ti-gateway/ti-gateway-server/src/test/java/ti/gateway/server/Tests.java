package ti.gateway.server;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.Test;
import ti.gateway.util.ApiGatewaySignUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Tests {

    @Test
    public void contextLoads() throws IOException {
            // 创建OKHttpClient对象
            OkHttpClient okHttpClient = new OkHttpClient();
            String appId = "1234567890";
            String timeStamp = String.valueOf(System.currentTimeMillis());
            String appsecret = "11111111";
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("X-GW-APPID", appId);
            queryParams.put("X-GW-Timestamp", timeStamp);
            queryParams.put("cc", "11111");
            queryParams.put("aa", "ttredfg");
            queryParams.put("sdfsdf", "eee");
            String sign = ApiGatewaySignUtils.generateSignature(appsecret, queryParams);

            // 创建一个Request
            final Request request = new Request.Builder()
                    .url("http://127.0.0.1:8083/MIDDLE-SERVICE-USER-SERVER/test?cc=11111&aa=ttredfg&sdfsdf=eee")
                    .addHeader("X-GW-APPID", appId)
                    .addHeader("X-GW-Timestamp", timeStamp)
                    .addHeader("X-GW-SIGN", sign)
                    .build();
            Call call = okHttpClient.newCall(request);
            // 返回值为response
            Response response = call.execute();
            // 将response转化成String
            String responseStr = response.body().string();
            System.out.println(responseStr);
    }
}
