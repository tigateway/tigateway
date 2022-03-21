package com.vmware.tanzu.springcloudgateway.routeconfig;

import com.vmware.tanzu.springcloudgateway.apis.EventRecorder;
import com.vmware.tanzu.springcloudgateway.apis.ObjectReferenceConverter;
import com.vmware.tanzu.springcloudgateway.gateway.PodLister;
import com.vmware.tanzu.springcloudgateway.mapping.MappingLister;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewayMapping;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewayRouteConfig;
import com.vmware.tanzu.springcloudgateway.route.RoutesDefinition;
import com.vmware.tanzu.springcloudgateway.route.RoutesDefinitionResolver;
import com.vmware.tanzu.springcloudgateway.route.UnprocessableRouteException;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.informer.SharedInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1ObjectReference;
import io.kubernetes.client.openapi.models.V1Pod;
import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RouteConfigReconciler implements Reconciler {
    private static final Logger LOG = LoggerFactory.getLogger(RouteConfigReconciler.class);
    private final SharedInformer<V1SpringCloudGatewayRouteConfig> informer;
    private final Lister<V1SpringCloudGatewayRouteConfig> lister;
    private final ActuatorRoutesUpdater actuatorRoutesUpdater;
    private final PodLister podLister;
    private final MappingLister mappingLister;
    private final EventRecorder eventRecorder;
    private final RoutesDefinitionResolver routesDefinitionResolver;

    public RouteConfigReconciler(SharedInformer<V1SpringCloudGatewayRouteConfig> informer, Lister<V1SpringCloudGatewayRouteConfig> lister, ActuatorRoutesUpdater actuatorRoutesUpdater, PodLister podLister, MappingLister mappingLister, EventRecorder eventRecorder, RoutesDefinitionResolver routesDefinitionResolver) {
        this.informer = informer;
        this.lister = lister;
        this.actuatorRoutesUpdater = actuatorRoutesUpdater;
        this.podLister = podLister;
        this.mappingLister = mappingLister;
        this.eventRecorder = eventRecorder;
        this.routesDefinitionResolver = routesDefinitionResolver;
    }

    public Result reconcile(Request request) {
        V1SpringCloudGatewayRouteConfig routeConfig = (V1SpringCloudGatewayRouteConfig)this.lister.namespace(request.getNamespace()).get(request.getName());
        LOG.debug("Expected routeConfig {}", routeConfig);
        Collection<V1SpringCloudGatewayMapping> mappings = this.mappingLister.listForRouteConfig(request.getNamespace(), request.getName());
        V1ObjectReference routeConfigRef = this.getRouteConfigRef(request, routeConfig);
        if (mappings.isEmpty()) {
            this.logMappingNotFoundEvent(routeConfigRef);
        }

        Iterator var5 = mappings.iterator();

        while(var5.hasNext()) {
            V1SpringCloudGatewayMapping mapping = (V1SpringCloudGatewayMapping)var5.next();

            try {
                RoutesDefinition routesDefinition = this.routesDefinitionResolver.getRoutes(mapping, routeConfig);
                Collection<V1Pod> pods = this.listGatewayPods(mapping);
                String eventReason = "";
                Iterator var10 = pods.iterator();

                while(var10.hasNext()) {
                    V1Pod pod = (V1Pod)var10.next();
                    String hostHeader = ActuatorRoutesUpdater.buildHostHeader(request, pod);

                    try {
                        if (routeConfig == null) {
                            this.actuatorRoutesUpdater.deleteMapping(pod, routesDefinition, hostHeader);
                            eventReason = "Deleted";
                        } else if (routeConfig.getMetadata().getGeneration() != null && routeConfig.getMetadata().getGeneration() != 1L) {
                            this.actuatorRoutesUpdater.updateMapping(pod, routesDefinition, hostHeader);
                            eventReason = "Updated";
                        } else {
                            this.actuatorRoutesUpdater.addMapping(pod, routesDefinition, hostHeader);
                            eventReason = "Created";
                        }
                    } catch (PodUpdateException var14) {
                        this.logFailureEvent(routeConfigRef, mapping, pod, var14);
                        return new Result(true);
                    }
                }

                if (pods.isEmpty()) {
                    this.logPodNotFoundEvent(routeConfigRef, mapping);
                } else {
                    this.logSuccessEvent(routeConfigRef, mapping, eventReason);
                }
            } catch (UnprocessableRouteException var15) {
                this.logFailureEvent(routeConfigRef, mapping, var15);
                return new Result(true);
            }
        }

        return new Result(false);
    }

    public boolean hasSynced() {
        return this.informer.hasSynced();
    }

    private V1ObjectReference getRouteConfigRef(Request request, V1SpringCloudGatewayRouteConfig routeConfig) {
        V1ObjectReference routeConfigRef;
        if (routeConfig == null) {
            routeConfigRef = ObjectReferenceConverter.toObjectReference((new V1SpringCloudGatewayRouteConfig()).metadata((new V1ObjectMeta()).namespace(request.getNamespace()).name(request.getName())));
        } else {
            routeConfigRef = ObjectReferenceConverter.toObjectReference(routeConfig);
        }

        return routeConfigRef;
    }

    private void logPodNotFoundEvent(V1ObjectReference routeConfigRef, V1SpringCloudGatewayMapping mapping) {
        String message = String.format("Specified SpringCloudGateway resource \"%s\" is not found / not ready", mapping.getSpec().getGatewayRef().getName());
        this.eventRecorder.logEvent(routeConfigRef, ObjectReferenceConverter.toObjectReference(mapping), "NotFound", message, EventType.Warning);
    }

    private void logMappingNotFoundEvent(V1ObjectReference routeConfigRef) {
        String message = "No mapping referencing this SpringCloudGatewayRouteConfig resource.";
        this.eventRecorder.logEvent(routeConfigRef, (V1ObjectReference)null, "NotFound", message, EventType.Warning);
    }

    private void logFailureEvent(V1ObjectReference routeConfigRef, V1SpringCloudGatewayMapping mapping, V1Pod pod, PodUpdateException e) {
        String message = String.format("Failed to update pod %s with mapping %s. Ensure all filters/predicates are available and correctly setup", pod.getMetadata().getName(), mapping.getMetadata().getName());
        LOG.error(message, e);
        this.eventRecorder.logEvent(routeConfigRef, ObjectReferenceConverter.toObjectReference(mapping), "RouteUpdateException", message + ": " + e.getMessage(), EventType.Warning);
    }

    private void logFailureEvent(V1ObjectReference routeConfigRef, V1SpringCloudGatewayMapping mapping, UnprocessableRouteException e) {
        String message = String.format("Failed to get routes for route config %s and mapping %s", routeConfigRef.getName(), mapping.getMetadata().getName());
        LOG.error(message, e);
        this.eventRecorder.logEvent(routeConfigRef, ObjectReferenceConverter.toObjectReference(mapping), "UnprocessableRouteException", message + ": " + e.getMessage(), EventType.Warning);
    }

    private void logSuccessEvent(V1ObjectReference routeConfigRef, V1SpringCloudGatewayMapping mapping, String reason) {
        String message = String.format("Successfully updated gateway according to SpringCloudGatewayMapping resources %s/%s", mapping.getMetadata().getNamespace(), mapping.getMetadata().getName());
        this.eventRecorder.logEvent(routeConfigRef, ObjectReferenceConverter.toObjectReference(mapping), reason, message, EventType.Normal);
    }

    private Collection<V1Pod> listGatewayPods(V1SpringCloudGatewayMapping mapping) {
        String gatewayName = mapping.getSpec().getGatewayRef().getName();
        String gatewayNamespace = mapping.getSpec().getGatewayRef().getNamespace();
        if (gatewayNamespace == null) {
            gatewayNamespace = mapping.getMetadata().getNamespace();
        }

        LOG.info("listing pods for gateway {} in namespace {}", gatewayName, gatewayNamespace);
        Collection<V1Pod> gatewayPods = this.podLister.listReadyPods(gatewayNamespace, gatewayName);
        LOG.info("Gateway Pods size {}", gatewayPods.size());
        return gatewayPods;
    }
}
