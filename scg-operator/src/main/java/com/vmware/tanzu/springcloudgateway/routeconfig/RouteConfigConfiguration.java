package com.vmware.tanzu.springcloudgateway.routeconfig;

import com.vmware.tanzu.springcloudgateway.apis.EventRecorder;
import com.vmware.tanzu.springcloudgateway.apis.LeaderElection;
import com.vmware.tanzu.springcloudgateway.gateway.OperatorProperties;
import com.vmware.tanzu.springcloudgateway.gateway.PodLister;
import com.vmware.tanzu.springcloudgateway.mapping.MappingLister;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewayRouteConfig;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewayRouteConfigList;
import com.vmware.tanzu.springcloudgateway.route.RoutesDefinitionResolver;
import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.LeaderElectingController;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.builder.DefaultControllerBuilder;
import io.kubernetes.client.extended.leaderelection.LeaderElector;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import java.time.Duration;
import java.util.Objects;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class RouteConfigConfiguration {
    private static final int DEFAULT_APPLICATION_MANAGEMENT_PORT = 8090;

    RouteConfigConfiguration() {
    }

    @Bean
    public Controller routeConfigController(SharedInformerFactory sharedInformerFactory, RouteConfigReconciler reconciler, ApiClient apiClient, OperatorProperties operatorProperties) {
        DefaultControllerBuilder var10000 = ControllerBuilder.defaultBuilder(sharedInformerFactory).watch((workQueue) -> {
            return ControllerBuilder.controllerWatchBuilder(V1SpringCloudGatewayRouteConfig.class, workQueue).withResyncPeriod(Duration.ofHours(1L)).build();
        }).withWorkerCount(2);
        Objects.requireNonNull(reconciler);
        Controller controller = var10000.withReadyFunc(reconciler::hasSynced).withReconciler(reconciler).withName("RouteConfigController").build();
        LeaderElector leaderElector = LeaderElection.configureLeaderElector(apiClient, operatorProperties, "route-config-reconciler");
        return new LeaderElectingController(leaderElector, controller);
    }

    @Bean
    RouteConfigReconciler routeConfigReconciler(SharedIndexInformer<V1SpringCloudGatewayRouteConfig> lister, PodLister podLister, ActuatorRoutesUpdater actuatorRoutesUpdater, MappingLister mappingLister, EventRecorder eventRecorder, RoutesDefinitionResolver routesDefinitionResolver) {
        return new RouteConfigReconciler(lister, new Lister<>(lister.getIndexer()), actuatorRoutesUpdater, podLister, mappingLister, eventRecorder, routesDefinitionResolver);
    }

    @Bean
    GenericKubernetesApi<V1SpringCloudGatewayRouteConfig, V1SpringCloudGatewayRouteConfigList> genericRouteConfigApi(ApiClient client) {
        return new GenericKubernetesApi(V1SpringCloudGatewayRouteConfig.class, V1SpringCloudGatewayRouteConfigList.class, "tanzu.vmware.com", "v1", "springcloudgatewayrouteconfigs", client);
    }

    @Bean
    public SharedIndexInformer<V1SpringCloudGatewayRouteConfig> routeConfigSharedInformer(ApiClient apiClient, SharedInformerFactory sharedInformerFactory, GenericKubernetesApi<V1SpringCloudGatewayRouteConfig, V1SpringCloudGatewayRouteConfigList> api) {
        return sharedInformerFactory.sharedIndexInformerFor(api, V1SpringCloudGatewayRouteConfig.class, 0L);
    }

    @Bean
    public ActuatorRoutesUpdater actuatorRoutesUpdater(EventRecorder eventRecorder) {
        return new ActuatorRoutesUpdater(8090, eventRecorder);
    }
}
