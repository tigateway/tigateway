package ti.gateway.operator.springcloudgateway.mapping;

import ti.gateway.operator.springcloudgateway.apis.EventRecorder;
import ti.gateway.operator.springcloudgateway.apis.LeaderElection;
import ti.gateway.operator.springcloudgateway.apis.TanzuVmwareComV1Api;
import ti.gateway.operator.springcloudgateway.gateway.OperatorProperties;
import ti.gateway.operator.springcloudgateway.gateway.PodLister;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayMapping;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayMappingList;
import ti.gateway.operator.springcloudgateway.route.RoutesDefinitionResolver;
import ti.gateway.operator.springcloudgateway.routeconfig.ActuatorRoutesUpdater;
import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.LeaderElectingController;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.builder.ControllerWatchBuilder;
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
class MappingConfiguration {
    MappingConfiguration() {
    }

    @Bean
    public Controller mappingController(SharedInformerFactory sharedInformerFactory, ApiClient apiClient, OperatorProperties operatorProperties, MappingReconciler reconciler) {
        DefaultControllerBuilder var10000 = ControllerBuilder.defaultBuilder(sharedInformerFactory).watch((workQueue) -> {
            ControllerWatchBuilder<V1SpringCloudGatewayMapping> v1SpringCloudGatewayMappingControllerWatchBuilder = ControllerBuilder.controllerWatchBuilder(V1SpringCloudGatewayMapping.class, workQueue);
            Objects.requireNonNull(reconciler);
            v1SpringCloudGatewayMappingControllerWatchBuilder = v1SpringCloudGatewayMappingControllerWatchBuilder.withOnUpdateFilter(reconciler::onUpdateFilter);
            Objects.requireNonNull(reconciler);
            return v1SpringCloudGatewayMappingControllerWatchBuilder.withOnDeleteFilter(reconciler::onDeleteFilter).withResyncPeriod(Duration.ofHours(1L)).build();
        }).withWorkerCount(2);
        Objects.requireNonNull(reconciler);
        Controller controller = var10000.withReadyFunc(reconciler::hasSynced).withReconciler(reconciler).withName("MappingController").build();
        LeaderElector leaderElector = LeaderElection.configureLeaderElector(apiClient, operatorProperties, "mapping-reconciler");
        return new LeaderElectingController(leaderElector, controller);
    }

    @Bean
    MappingReconciler mappingReconciler(SharedIndexInformer<V1SpringCloudGatewayMapping> indexer, PodLister podLister, ActuatorRoutesUpdater actuatorRoutesUpdater, MappingFinalizerEditor finalizerEditor, EventRecorder eventRecorder, RoutesDefinitionResolver routesDefinitionResolver) {
        return new MappingReconciler(indexer, new Lister<>(indexer.getIndexer()), podLister, actuatorRoutesUpdater, finalizerEditor, eventRecorder, routesDefinitionResolver);
    }

    @Bean
    GenericKubernetesApi<V1SpringCloudGatewayMapping, V1SpringCloudGatewayMappingList> genericMappingApi(ApiClient client) {
        return new GenericKubernetesApi(V1SpringCloudGatewayMapping.class, V1SpringCloudGatewayMappingList.class, "tanzu.vmware.com", "v1", "springcloudgatewaymappings", client);
    }

    @Bean
    public SharedIndexInformer<V1SpringCloudGatewayMapping> mappingSharedInformer(ApiClient apiClient, SharedInformerFactory sharedInformerFactory, GenericKubernetesApi<V1SpringCloudGatewayMapping, V1SpringCloudGatewayMappingList> api) {
        return sharedInformerFactory.sharedIndexInformerFor(api, V1SpringCloudGatewayMapping.class, 0L);
    }

    @Bean
    MappingFinalizerEditor mappingFinalizerEditor(TanzuVmwareComV1Api mappingV1Api) {
        return new MappingFinalizerEditor(mappingV1Api);
    }

    @Bean
    MappingLister mappingLister(TanzuVmwareComV1Api mappingV1Api) {
        return new MappingLister(mappingV1Api);
    }
}
