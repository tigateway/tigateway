package ti.gateway.dashboard.swagger.compatible;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.reactive.result.method.RequestMappingInfoHandlerMapping;

import java.lang.reflect.Field;
import java.util.List;

/**
 * swagger 在 springboot 2.6.x 不兼容问题的处理
 */
@SuppressWarnings("all")
public class SwaggerBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
//        if (bean instanceof WebMvcRequestHandlerProvider || bean instanceof WebFluxRequestHandlerProvider) {
//            customizeSpringfoxHandlerMappings(getHandlerMappings(bean));
//        }
        return bean;
    }


    private <T extends RequestMappingInfoHandlerMapping> void customizeSpringfoxHandlerMappings(List<T> mappings) {
        mappings.removeIf(mapping -> mapping.getPathPatternParser() != null);
    }

    private List<RequestMappingInfoHandlerMapping> getHandlerMappings(Object bean) {
        try {
            Field field = ReflectionUtils.findField(bean.getClass(), "handlerMappings");
            field.setAccessible(true);
            return (List<RequestMappingInfoHandlerMapping>) field.get(bean);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

}
