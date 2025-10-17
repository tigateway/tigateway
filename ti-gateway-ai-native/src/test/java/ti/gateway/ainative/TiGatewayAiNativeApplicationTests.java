package ti.gateway.ainative;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * TiGateway AI Native Application Tests
 * 
 * @author TiGateway Team
 * @version 1.0.0
 */
@SpringBootTest(
    classes = {TiGatewayAiNativeApplication.class},
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ActiveProfiles("test")
class TiGatewayAiNativeApplicationTests {

    @Test
    void contextLoads() {
        // 测试Spring上下文是否能正常加载
        // 这个测试验证了ServerCodecConfigurer bean的配置是否正确
    }
}
