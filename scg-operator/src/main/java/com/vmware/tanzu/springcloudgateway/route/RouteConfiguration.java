package com.vmware.tanzu.springcloudgateway.route;

import com.vmware.tanzu.springcloudgateway.apis.TanzuVmwareComV1Api;
import com.vmware.tanzu.springcloudgateway.gateway.SecretLister;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RouteConfiguration {
    public RouteConfiguration() {
    }

    @Bean
    ApiServiceUriBuilder apiServiceUriBuilder(CoreV1Api coreV1Api) {
        return new ApiServiceUriBuilder(coreV1Api);
    }

    @Bean
    RoutesDefinitionResolver routesDefinitionResolver(TanzuVmwareComV1Api scgApi, ApiServiceUriBuilder serviceUriBuilder, SecretLister secretLister) {
        return new RoutesDefinitionResolver(scgApi, serviceUriBuilder, secretLister);
    }
}
