package ti.gateway.dashboard.swagger.config;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
//import springfox.documentation.RequestHandler;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.service.*;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spi.service.contexts.SecurityContext;
//import springfox.documentation.spring.web.plugins.ApiSelectorBuilder;
//import springfox.documentation.spring.web.plugins.Docket;
import ti.gateway.dashboard.swagger.config.properties.SwaggerProperties;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Swagger 文档配置
 */
//@Configuration
//@EnableConfigurationProperties(SwaggerProperties.class)
//@ConditionalOnProperty(name = "swagger.enabled", matchIfMissing = true)
@Slf4j
public class SwaggerAutoConfiguration {

    @Value("${spring.application.name}")
    private String appName;

    /**
     * 默认的排除路径，排除Spring Boot默认的错误处理路径和端点
     */
    private static final List<String> DEFAULT_EXCLUDE_PATH = Arrays.asList("/error", "/actuator/**");

    private static final String BASE_PATH = "/**";

//    @Bean
//    public Docket customDocket(SwaggerProperties swaggerProperties) {
//        log.info("spring.mvc.pathmatch.matchingStrategy: {}", System.getProperty("spring.mvc.pathmatch.matchingStrategy"));
//
//        // base-path处理
//        if (swaggerProperties.getBasePath().isEmpty()) {
//            swaggerProperties.getBasePath().add(BASE_PATH);
//        }
//        // noinspection unchecked
//        List<Predicate<String>> basePath = new ArrayList<>();
//        swaggerProperties.getBasePath().forEach(path -> basePath.add(PathSelectors.ant(path)));
//
//        // exclude-path处理
//        if (swaggerProperties.getExcludePath().isEmpty()) {
//            swaggerProperties.getExcludePath().addAll(DEFAULT_EXCLUDE_PATH);
//        }
//
//        List<Predicate<String>> excludePath = new ArrayList<>();
//        swaggerProperties.getExcludePath().forEach(path -> excludePath.add(PathSelectors.ant(path)));
//
//        String[] basePackage = (swaggerProperties.getBasePackage().contains(","))
//                ? swaggerProperties.getBasePackage().split(",")
//                : new String[]{swaggerProperties.getBasePackage()};
//
//        ApiSelectorBuilder builder = new Docket(DocumentationType.OAS_30)
//                .enable(swaggerProperties.getEnabled())
//                .host(swaggerProperties.getHost())
//                .apiInfo(apiInfo(swaggerProperties))
//                .groupName(swaggerProperties.getDefaultGroupName())
//                .select()
//                .paths(PathSelectors.any())
//                .apis(basePackage(basePackage));
//
//        swaggerProperties.getBasePath()
//                .forEach(p -> builder.paths(PathSelectors.ant(p)));
//        swaggerProperties.getExcludePath()
//                .forEach(p -> builder.paths(PathSelectors.ant(p).negate()));
//
//        return builder.build()
//                /* 设置安全模式，swagger可以设置访问token */
//                .securitySchemes(securitySchemes())
//                .securityContexts(securityContexts());
//    }
//    // 设置多路径
//    private static Predicate<RequestHandler> basePackage(String... basePackages) {
//        return input -> declaringClass(input).transform(handlerPackage(basePackages)).or(true);
//    }

//    private static Optional<? extends Class<?>> declaringClass(RequestHandler input) {
//        return Optional.fromNullable(input.declaringClass());
//    }

    private static Function<Class<?>, Boolean> handlerPackage(String[] basePackage)     {
        return input -> {
            // 循环判断匹配
            for (String strPackage : basePackage) {
                boolean isMatch = input.getPackage().getName().startsWith(strPackage);
                if (isMatch) {
                    return true;
                }
            }
            return false;
        };
    }
//    private ApiInfo apiInfo(SwaggerProperties swaggerProperties) {
//        return new ApiInfoBuilder()
//                .title(swaggerProperties.getTitle())
//                .description(swaggerProperties.getDescription())
//                .license(swaggerProperties.getLicense())
//                .licenseUrl(swaggerProperties.getLicenseUrl())
//                .termsOfServiceUrl(swaggerProperties.getTermsOfServiceUrl())
//                .contact(new Contact(swaggerProperties.getContact().getName(), swaggerProperties.getContact().getUrl(), swaggerProperties.getContact().getEmail()))
//                .version(swaggerProperties.getVersion())
//                .build();
//    }


    /**
     * 安全模式，这里指定token通过Authorization头请求头传递
     */
//    private List<SecurityScheme> securitySchemes() {
//        List<SecurityScheme> apiKeyList = new ArrayList<>();
//        apiKeyList.add(new ApiKey("Authorization", "Authorization", In.HEADER.toValue()));
//        return apiKeyList;
//    }

    /**
     * 安全上下文
     */
//    private List<SecurityContext> securityContexts() {
//        List<SecurityContext> securityContexts = new ArrayList<>();
//        securityContexts.add(
//                SecurityContext.builder()
//                        .securityReferences(defaultAuth())
//                        .operationSelector(o -> o.requestMappingPattern().matches("/.*"))
//                        .build());
//        return securityContexts;
//    }

    /**
     * 默认的安全上引用
     */
//    private List<SecurityReference> defaultAuth() {
//        AuthorizationScope authorizationScope = new AuthorizationScope("global", "accessEverything");
//        AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
//        authorizationScopes[0] = authorizationScope;
//        List<SecurityReference> securityReferences = new ArrayList<>();
//        securityReferences.add(new SecurityReference("Authorization", authorizationScopes));
//        return securityReferences;
//    }


}
