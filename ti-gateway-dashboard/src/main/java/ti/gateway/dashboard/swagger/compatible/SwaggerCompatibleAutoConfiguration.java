package ti.gateway.dashboard.swagger.compatible;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

//@Configuration
//@ConditionalOnWebApplication
//@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = true)
//@Import({
//        SwaggerBeanPostProcessor.class
//})
//@AutoConfigureOrder(0)
public class SwaggerCompatibleAutoConfiguration {

    static  {
        // 适配 boot 2.6 路由与 springfox 兼容
        System.setProperty("spring.mvc.pathmatch.matchingStrategy", "ANT_PATH_MATCHER");
    }

}
