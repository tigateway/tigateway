package ti.gateway.operator.springcloudgateway.gateway;

import com.coreos.monitoring.models.V1ServiceMonitor;
import com.coreos.monitoring.models.V1ServiceMonitorList;
import ti.gateway.operator.springcloudgateway.apis.EventRecorder;
import ti.gateway.operator.springcloudgateway.apis.LabelsBuilder;
import ti.gateway.operator.springcloudgateway.apis.LeaderElection;
import ti.gateway.operator.springcloudgateway.apis.TanzuVmwareComV1Api;
import ti.gateway.operator.springcloudgateway.mapping.MappingLister;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayList;
import ti.gateway.operator.springcloudgateway.route.RoutesDefinitionResolver;
import ti.gateway.operator.springcloudgateway.routeconfig.ActuatorRoutesUpdater;
import ti.gateway.operator.springcloudgateway.util.PatchUtilsWrapper;
import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.LeaderElectingController;
import io.kubernetes.client.extended.controller.builder.ControllerBuilder;
import io.kubernetes.client.extended.controller.builder.ControllerWatchBuilder;
import io.kubernetes.client.extended.controller.builder.DefaultControllerBuilder;
import io.kubernetes.client.extended.leaderelection.LeaderElector;
import io.kubernetes.client.informer.SharedIndexInformer;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiCallback;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.openapi.models.V1ConfigMap;
import io.kubernetes.client.openapi.models.V1ConfigMapList;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimList;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import io.kubernetes.client.openapi.models.V1StatefulSetList;
import io.kubernetes.client.util.generic.GenericKubernetesApi;
import java.time.Duration;
import java.util.Collection;
import java.util.Objects;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({OperatorProperties.class})
class GatewayConfiguration {
    GatewayConfiguration() {
    }

    @Bean
    StatefulSetBuilder statefulSetBuilder(OperatorProperties operatorProperties, AppsV1Api appsV1Api, SsoParameters ssoParameters, MetricsParameters metricsParameters, TracingParameters tracingParameters, EventRecorder eventRecorder, Collection<StatefulSetContributor> statefulSetContributors) {
        return new StatefulSetBuilder(operatorProperties.getGatewayImageName(), appsV1Api, ssoParameters, metricsParameters, tracingParameters, operatorProperties.getImagePullSecretName(), eventRecorder, statefulSetContributors);
    }

    @Bean
    RbacBuilder rbacBuilder(CoreV1Api coreV1Api, RbacAuthorizationV1Api authorizationApi) {
        return new RbacBuilder(coreV1Api, authorizationApi);
    }

    @Bean
    Lister<V1ConfigMap> configMapLister(SharedIndexInformer<V1ConfigMap> indexer) {
        return new Lister<>(indexer.getIndexer());
    }

    @Bean
    PrometheusServiceMonitorBuilder prometheusServiceMonitorBuilder(MetricsParameters metricsParameters) {
        return new PrometheusServiceMonitorBuilder(metricsParameters);
    }

    @Bean
    SsoParameters ssoParameters(CoreV1Api coreV1Api) {
        return new SsoParameters(coreV1Api);
    }

    @Bean
    MetricsParameters metricsParameters(SecretLister secretLister) {
        return new MetricsParameters(secretLister);
    }

    @Bean
    TracingParameters tracingParameters(SecretLister secretLister) {
        return new TracingParameters(secretLister);
    }

    @Bean
    public Controller springCloudGatewayController(SharedInformerFactory sharedInformerFactory, ApiClient apiClient, SpringCloudGatewayReconciler reconciler, OperatorProperties operatorProperties, SharedIndexInformer<V1SpringCloudGateway> gatewayindexInformer, SharedIndexInformer<V1StatefulSet> statefulSetIndexInformer, SharedIndexInformer<V1Service> serviceIndexInformer, SharedIndexInformer<V1Pod> podIndexInformer, SharedIndexInformer<V1ConfigMap> configMapIndexInformer) {
        Controller controller = ControllerBuilder.defaultBuilder(sharedInformerFactory).withReconciler(reconciler).withName("SpringCloudGatewayController").withWorkerCount(4).withReadyFunc(() -> {
            return gatewayindexInformer.hasSynced() && statefulSetIndexInformer.hasSynced() && serviceIndexInformer.hasSynced() && podIndexInformer.hasSynced() && configMapIndexInformer.hasSynced();
        }).watch((q) -> {
            ControllerWatchBuilder<V1SpringCloudGateway> v1SpringCloudGatewayControllerWatchBuilder = ControllerBuilder.controllerWatchBuilder(V1SpringCloudGateway.class, q);
            Objects.requireNonNull(reconciler);
            v1SpringCloudGatewayControllerWatchBuilder = v1SpringCloudGatewayControllerWatchBuilder.withOnUpdateFilter(reconciler::onUpdateFilter);
            Objects.requireNonNull(reconciler);
            return v1SpringCloudGatewayControllerWatchBuilder.withOnDeleteFilter(reconciler::onDeleteFilter).build();
        }).build();
        LeaderElector leaderElector = LeaderElection.configureLeaderElector(apiClient, operatorProperties, "gateway-reconciler");
        return new LeaderElectingController(leaderElector, controller);
    }

    @Bean
    GenericKubernetesApi<V1SpringCloudGateway, V1SpringCloudGatewayList> genericGatewayApi(ApiClient client) {
        return new GenericKubernetesApi(V1SpringCloudGateway.class, V1SpringCloudGatewayList.class, "tanzu.vmware.com", "v1", "springcloudgateways", client);
    }

    @Bean
    SpringCloudGatewayReconciler springCloudGatewayReconciler(SharedIndexInformer<V1SpringCloudGateway> gatewayindexInformer, SharedIndexInformer<V1StatefulSet> statefulSetIndexInformer, SharedIndexInformer<V1Service> serviceIndexInformer, SharedIndexInformer<V1Pod> podIndexInformer, StatefulSetBuilder statefulSetBuilder, StatefulSetPatchBuilder statefulSetPatchBuilder, ServiceBuilder serviceBuilder, CoreV1Api coreV1Api, AppsV1Api appsV1Api, RbacBuilder rbacBuilder, EventRecorder eventRecorder, GatewayStatusEditor gatewayStatusEditor, OperatorProperties operatorProperties, ServiceMonitorReconciler serviceMonitorReconciler) {
        Lister<V1SpringCloudGateway> gatewayLister = new Lister<>(gatewayindexInformer.getIndexer());
        Lister<V1StatefulSet> statefulSetLister = new Lister<>(statefulSetIndexInformer.getIndexer());
        Lister<V1Service> serviceLister = new Lister<>(serviceIndexInformer.getIndexer());
        Lister<V1Pod> podLister = new Lister<>(podIndexInformer.getIndexer());
        return new SpringCloudGatewayReconciler(gatewayLister, statefulSetLister, statefulSetBuilder, statefulSetPatchBuilder, serviceBuilder, rbacBuilder, serviceLister, podLister, coreV1Api, appsV1Api, eventRecorder, gatewayStatusEditor, operatorProperties, serviceMonitorReconciler);
    }

    @Bean
    ServiceMonitorReconciler serviceMonitorReconciler(CustomObjectsApi customObjectsApi, PrometheusServiceMonitorBuilder serviceMonitorBuilder, SharedIndexInformer<V1ServiceMonitor> serviceMonitorSharedIndexInformer, EventRecorder eventRecorder) {
        return new ServiceMonitorReconciler(customObjectsApi, serviceMonitorBuilder, serviceMonitorSharedIndexInformer, eventRecorder);
    }

    @Bean
    PodLister readyPodLister(CoreV1Api coreV1Api) {
        return new PodLister(coreV1Api);
    }

    @Bean
    SecretLister secretLister(CoreV1Api coreV1Api) {
        return new SecretLister(coreV1Api);
    }

    @Bean
    public Controller podController(SharedInformerFactory sharedInformerFactory, PodReconciler reconciler, ApiClient apiClient, OperatorProperties operatorProperties) {
        DefaultControllerBuilder var10000 = ControllerBuilder.defaultBuilder(sharedInformerFactory).watch((workQueue) -> {
            ControllerWatchBuilder<V1Pod> v1PodControllerWatchBuilder = ControllerBuilder.controllerWatchBuilder(V1Pod.class, workQueue).withResyncPeriod(Duration.ofHours(1L));
            Objects.requireNonNull(reconciler);
            v1PodControllerWatchBuilder = v1PodControllerWatchBuilder.withOnUpdateFilter(reconciler::onUpdateFilter);
            Objects.requireNonNull(reconciler);
            v1PodControllerWatchBuilder = v1PodControllerWatchBuilder.withOnDeleteFilter(reconciler::onDeleteFilter);
            Objects.requireNonNull(reconciler);
            return v1PodControllerWatchBuilder.withOnAddFilter(reconciler::onAddFilter).build();
        }).withWorkerCount(2);
        Objects.requireNonNull(reconciler);
        Controller controller = var10000.withReadyFunc(reconciler::hasSynced).withReconciler(reconciler).withName("PodController").build();
        LeaderElector leaderElector = LeaderElection.configureLeaderElector(apiClient, operatorProperties, "pod-reconciler");
        return new LeaderElectingController(leaderElector, controller);
    }

    @Bean
    PodReconciler podReconciler(SharedIndexInformer<V1Pod> indexer, MappingLister mapping, ActuatorRoutesUpdater actuatorRoutesUpdater, RoutesDefinitionResolver routesDefinitionResolver, PodStatusEditor podStatusEditor, EventRecorder eventRecorder) {
        Lister<V1Pod> lister = new Lister<>(indexer.getIndexer());
        return new PodReconciler(indexer, lister, mapping, actuatorRoutesUpdater, podStatusEditor, routesDefinitionResolver, eventRecorder);
    }

    @Bean
    PodStatusEditor podStatusEditor(CoreV1Api coreV1Api) {
        return new PodStatusEditor(coreV1Api);
    }

    @Bean
    Controller configMapController(SharedInformerFactory sharedInformerFactory, ConfigMapReconciler reconciler, ApiClient apiClient, OperatorProperties operatorProperties) {
        DefaultControllerBuilder var10000 = ControllerBuilder.defaultBuilder(sharedInformerFactory).watch((workQueue) -> {
            ControllerWatchBuilder<V1ConfigMap> v1ConfigMapControllerWatchBuilder = ControllerBuilder.controllerWatchBuilder(V1ConfigMap.class, workQueue).withResyncPeriod(Duration.ofHours(1L));
            Objects.requireNonNull(reconciler);
            v1ConfigMapControllerWatchBuilder = v1ConfigMapControllerWatchBuilder.withOnUpdateFilter(reconciler::onUpdateFilter);
            Objects.requireNonNull(reconciler);
            v1ConfigMapControllerWatchBuilder = v1ConfigMapControllerWatchBuilder.withOnDeleteFilter(reconciler::onDeleteFilter);
            Objects.requireNonNull(reconciler);
            return v1ConfigMapControllerWatchBuilder.withOnAddFilter(reconciler::onAddFilter).build();
        }).withWorkerCount(2);
        Objects.requireNonNull(reconciler);
        Controller controller = var10000.withReadyFunc(reconciler::hasSynced).withReconciler(reconciler).withName("ConfigMapController").build();
        LeaderElector leaderElector = LeaderElection.configureLeaderElector(apiClient, operatorProperties, "configmap-reconciler");
        return new LeaderElectingController(leaderElector, controller);
    }

    @Bean
    ConfigMapReconciler configMapReconciler(SharedIndexInformer<V1ConfigMap> configMapIndexInformer, SharedIndexInformer<V1SpringCloudGateway> gatewayIndexInformer, AppsV1Api appsV1Api, EventRecorder eventRecorder, PatchUtilsWrapper patchUtilsWrapper) {
        return new ConfigMapReconciler(configMapIndexInformer, gatewayIndexInformer, appsV1Api, eventRecorder, patchUtilsWrapper);
    }

    @Bean
    SharedIndexInformer<V1Pod> v1PodSharedInformer(SharedInformerFactory factory, CoreV1Api coreV1Api) {
        return factory.sharedIndexInformerFor((params) -> {
            return coreV1Api.listPodForAllNamespacesCall((Boolean)null, (String)null, (String)null, LabelsBuilder.gatewayAppLabel(), (Integer)null, (String)null, params.resourceVersion, (String)null, params.timeoutSeconds, params.watch, (ApiCallback)null);
        }, V1Pod.class, V1PodList.class);
    }

    @Bean
    public SharedIndexInformer<V1SpringCloudGateway> springCloudGatewaySharedInformer(SharedInformerFactory sharedInformerFactory, GenericKubernetesApi<V1SpringCloudGateway, V1SpringCloudGatewayList> api) {
        return sharedInformerFactory.sharedIndexInformerFor(api, V1SpringCloudGateway.class, 0L);
    }

    @Bean
    public SharedIndexInformer<V1StatefulSet> statefulSetIndexInformer(AppsV1Api appsV1Api, SharedInformerFactory factory) {
        return factory.sharedIndexInformerFor((params) -> {
            return appsV1Api.listStatefulSetForAllNamespacesCall((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, params.resourceVersion, (String)null, params.timeoutSeconds, params.watch, (ApiCallback)null);
        }, V1StatefulSet.class, V1StatefulSetList.class, 10L);
    }

    @Bean
    public SharedIndexInformer<V1Service> serviceIndexInformer(CoreV1Api appsV1Api, SharedInformerFactory factory) {
        return factory.sharedIndexInformerFor((params) -> {
            return appsV1Api.listServiceForAllNamespacesCall((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, params.resourceVersion, (String)null, params.timeoutSeconds, params.watch, (ApiCallback)null);
        }, V1Service.class, V1ServiceList.class, 10L);
    }

    @Bean
    public SharedIndexInformer<V1PersistentVolumeClaim> persistentVolumeClaimIndexInformer(CoreV1Api appsV1Api, SharedInformerFactory factory) {
        return factory.sharedIndexInformerFor((params) -> {
            return appsV1Api.listPersistentVolumeClaimForAllNamespacesCall((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, params.resourceVersion, (String)null, params.timeoutSeconds, params.watch, (ApiCallback)null);
        }, V1PersistentVolumeClaim.class, V1PersistentVolumeClaimList.class, 10L);
    }

    @Bean
    public SharedIndexInformer<V1ConfigMap> configMapIndexInformer(CoreV1Api appsV1Api, SharedInformerFactory factory) {
        return factory.sharedIndexInformerFor((params) -> {
            return appsV1Api.listConfigMapForAllNamespacesCall((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, params.resourceVersion, (String)null, params.timeoutSeconds, params.watch, (ApiCallback)null);
        }, V1ConfigMap.class, V1ConfigMapList.class, 10L);
    }

    @Bean
    public SharedIndexInformer<V1ServiceMonitor> serviceMonitorSharedIndexInformer(CustomObjectsApi customObjectsApi, SharedInformerFactory factory) {
        return factory.sharedIndexInformerFor((params) -> {
            return customObjectsApi.listClusterCustomObjectCall("monitoring.coreos.com", "v1", "servicemonitors", (String)null, (String)null, (String)null, (String)null, (Integer)null, params.resourceVersion, params.timeoutSeconds, params.watch, (ApiCallback)null);
        }, V1ServiceMonitor.class, V1ServiceMonitorList.class, 10L);
    }

    @Bean
    GatewayStatusEditor gatewayStatusEditor(TanzuVmwareComV1Api tanzuVmwareComV1Api) {
        return new GatewayStatusEditor(tanzuVmwareComV1Api);
    }
}
