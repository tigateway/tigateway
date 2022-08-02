package ti.gateway.operator.springcloudgateway.gateway;

import ti.gateway.operator.springcloudgateway.apis.EventRecorder;
import ti.gateway.operator.springcloudgateway.apis.ObjectReferenceConverter;
import ti.gateway.operator.springcloudgateway.mapping.MappingLister;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayMapping;
import ti.gateway.operator.springcloudgateway.route.RoutesDefinition;
import ti.gateway.operator.springcloudgateway.route.RoutesDefinitionResolver;
import ti.gateway.operator.springcloudgateway.route.UnprocessableRouteException;
import ti.gateway.operator.springcloudgateway.routeconfig.ActuatorRoutesUpdater;
import ti.gateway.operator.springcloudgateway.routeconfig.PodUpdateException;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.informer.SharedInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1ObjectReference;
import io.kubernetes.client.openapi.models.V1Pod;
import java.util.Collection;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodReconciler implements Reconciler {
    private static final Logger LOG = LoggerFactory.getLogger(PodReconciler.class);
    private final SharedInformer<V1Pod> podInformer;
    private final ActuatorRoutesUpdater actuatorRoutesUpdater;
    private final MappingLister mappingLister;
    private final Lister<V1Pod> lister;
    private final PodStatusEditor podStatusEditor;
    private final EventRecorder eventRecorder;
    private final RoutesDefinitionResolver routesDefinitionResolver;

    public PodReconciler(SharedInformer<V1Pod> podInformer, Lister<V1Pod> lister, MappingLister mappingLister, ActuatorRoutesUpdater actuatorRoutesUpdater, PodStatusEditor podStatusEditor, RoutesDefinitionResolver routesDefinitionResolver, EventRecorder eventRecorder) {
        this.podInformer = podInformer;
        this.lister = lister;
        this.mappingLister = mappingLister;
        this.actuatorRoutesUpdater = actuatorRoutesUpdater;
        this.podStatusEditor = podStatusEditor;
        this.routesDefinitionResolver = routesDefinitionResolver;
        this.eventRecorder = eventRecorder;
    }

    public Result reconcile(Request request) {
        LOG.info("searching for Pod {} in namespace {}", request.getName(), request.getNamespace());
        V1Pod pod = (V1Pod)this.lister.namespace(request.getNamespace()).get(request.getName());
        if (pod == null) {
            return new Result(false);
        } else {
            String gatewayName = (String)pod.getMetadata().getLabels().get("gateway.name");
            Collection<V1SpringCloudGatewayMapping> mappings = this.mappingLister.listForGateway(request.getNamespace(), gatewayName);
            Iterator var5 = mappings.iterator();

            while(var5.hasNext()) {
                V1SpringCloudGatewayMapping mapping = (V1SpringCloudGatewayMapping)var5.next();
                String hostHeader = ActuatorRoutesUpdater.buildHostHeader(request, pod);

                try {
                    RoutesDefinition routesDefinition = this.routesDefinitionResolver.getRoutes(mapping);

                    try {
                        this.actuatorRoutesUpdater.updateMapping(pod, routesDefinition, hostHeader);
                    } catch (PodUpdateException var10) {
                        this.logFailureEvent(pod, mapping, var10);
                        this.podStatusEditor.setRoutesUpToDateCondition(pod, false);
                        return new Result(true);
                    }
                } catch (UnprocessableRouteException var11) {
                    this.logFailureEvent(pod, mapping, var11);
                    this.podStatusEditor.setRoutesUpToDateCondition(pod, false);
                    return new Result(true);
                }
            }

            this.logSuccessEvent(pod, "RoutesUpToDate");
            this.podStatusEditor.setRoutesUpToDateCondition(pod, true);
            return new Result(false);
        }
    }

    public boolean hasSynced() {
        return this.podInformer.hasSynced();
    }

    public boolean onAddFilter(V1Pod pod) {
        return false;
    }

    public boolean onUpdateFilter(V1Pod oldPod, V1Pod newPod) {
        if (oldPod.getMetadata().getName().startsWith("scg-operator")) {
            return false;
        } else {
            Boolean oldPodReady = getPodContainerReady(oldPod);
            Boolean newPodReady = getPodContainerReady(newPod);
            return !oldPodReady && newPodReady;
        }
    }

    public boolean onDeleteFilter(V1Pod pod, boolean deletedFinalStateUnknown) {
        return false;
    }

    private static Boolean getPodContainerReady(V1Pod pod) {
        return pod.getStatus() != null && pod.getStatus().getContainerStatuses() != null && !pod.getStatus().getContainerStatuses().isEmpty() ? ((V1ContainerStatus)pod.getStatus().getContainerStatuses().get(0)).getReady() : false;
    }

    private void logFailureEvent(V1Pod pod, V1SpringCloudGatewayMapping mapping, PodUpdateException e) {
        String message = String.format("Failed to update pod %s with mapping %s. Ensure all filters/predicates are available and correctly setup", pod.getMetadata().getName(), mapping.getMetadata().getName());
        LOG.error(message, e);
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(pod), ObjectReferenceConverter.toObjectReference(mapping), "RouteUpdateException", message + ": " + e.getMessage(), EventType.Warning);
    }

    private void logFailureEvent(V1Pod pod, V1SpringCloudGatewayMapping mapping, UnprocessableRouteException e) {
        String message = String.format("Failed to convert routes in mapping %s to pod %s", mapping.getMetadata().getName(), pod.getMetadata().getName());
        LOG.error(message, e);
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(pod), ObjectReferenceConverter.toObjectReference(mapping), "UnprocessableRouteException", message + ": " + e.getMessage(), EventType.Warning);
    }

    private void logSuccessEvent(V1Pod pod, String reason) {
        String message = String.format("Pod \"%s/%s\" is %s with all routes", pod.getMetadata().getNamespace(), pod.getMetadata().getName(), reason);
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(pod), (V1ObjectReference)null, reason, message, EventType.Normal);
    }
}

