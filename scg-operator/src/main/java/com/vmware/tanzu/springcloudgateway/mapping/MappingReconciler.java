package com.vmware.tanzu.springcloudgateway.mapping;

import com.vmware.tanzu.springcloudgateway.apis.EventRecorder;
import com.vmware.tanzu.springcloudgateway.apis.ObjectReferenceConverter;
import com.vmware.tanzu.springcloudgateway.gateway.PodLister;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewayMapping;
import com.vmware.tanzu.springcloudgateway.route.RoutesDefinition;
import com.vmware.tanzu.springcloudgateway.route.RoutesDefinitionResolver;
import com.vmware.tanzu.springcloudgateway.route.UnprocessableRouteException;
import com.vmware.tanzu.springcloudgateway.routeconfig.ActuatorRoutesUpdater;
import com.vmware.tanzu.springcloudgateway.routeconfig.PodUpdateException;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.informer.SharedInformer;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ObjectReference;
import io.kubernetes.client.openapi.models.V1Pod;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MappingReconciler implements Reconciler {
    private static final Logger LOG = LoggerFactory.getLogger(MappingReconciler.class);
    private final SharedInformer<V1SpringCloudGatewayMapping> informer;
    private final Lister<V1SpringCloudGatewayMapping> lister;
    private final PodLister podLister;
    private final ActuatorRoutesUpdater actuatorRoutesUpdater;
    private final MappingFinalizerEditor finalizerEditor;
    private final EventRecorder eventRecorder;
    private final RoutesDefinitionResolver routesDefinitionResolver;

    public MappingReconciler(SharedInformer<V1SpringCloudGatewayMapping> informer, Lister<V1SpringCloudGatewayMapping> lister, PodLister podLister, ActuatorRoutesUpdater actuatorRoutesUpdater, MappingFinalizerEditor finalizerEditor, EventRecorder eventRecorder, RoutesDefinitionResolver routesDefinitionResolver) {
        this.informer = informer;
        this.lister = lister;
        this.podLister = podLister;
        this.actuatorRoutesUpdater = actuatorRoutesUpdater;
        this.finalizerEditor = finalizerEditor;
        this.eventRecorder = eventRecorder;
        this.routesDefinitionResolver = routesDefinitionResolver;
    }

    public Result reconcile(Request request) {
        V1SpringCloudGatewayMapping mapping = (V1SpringCloudGatewayMapping)this.lister.namespace(request.getNamespace()).get(request.getName());
        LOG.debug("Expected mapping configuration {}", mapping);
        if (mapping == null) {
            return new Result(false);
        } else {
            boolean toAdd = mapping.getMetadata().getGeneration() == null || mapping.getMetadata().getGeneration() == 1L;
            boolean toUpdate = mapping.getMetadata().getGeneration() != null && mapping.getMetadata().getGeneration() > 1L;
            boolean toDelete = mapping.getMetadata().getDeletionTimestamp() != null;
            Collection<V1Pod> gatewayPods = this.listGatewayPods(mapping);
            if (gatewayPods.isEmpty()) {
                this.logPodNotFoundEvent(request, mapping);
            }

            try {
                RoutesDefinition routesDefinition = this.routesDefinitionResolver.getRoutes(mapping);
                Iterator var8 = gatewayPods.iterator();

                while(var8.hasNext()) {
                    V1Pod pod = (V1Pod)var8.next();

                    try {
                        String hostHeader = ActuatorRoutesUpdater.buildHostHeader(request, pod);
                        if (toDelete) {
                            this.actuatorRoutesUpdater.deleteMapping(pod, routesDefinition, hostHeader);
                            this.logSuccessEvent(mapping, pod, "Deleted");
                        } else if (toUpdate) {
                            this.actuatorRoutesUpdater.updateMapping(pod, routesDefinition, hostHeader);
                            this.logSuccessEvent(mapping, pod, "Updated");
                        } else if (toAdd) {
                            this.actuatorRoutesUpdater.addMapping(pod, routesDefinition, hostHeader);
                            this.logSuccessEvent(mapping, pod, "Created");
                        } else {
                            LOG.error("Illegal state: received a request {} with nothing to do", request);
                        }
                    } catch (PodUpdateException var12) {
                        this.logFailureEvent(mapping, pod, var12);
                        return new Result(true);
                    }
                }
            } catch (UnprocessableRouteException var13) {
                this.logFailureEvent(mapping, var13);
                return new Result(true);
            }

            try {
                if (toDelete) {
                    this.finalizerEditor.remove(mapping);
                } else if (this.finalizerNotFound(mapping)) {
                    this.finalizerEditor.add(mapping);
                }
            } catch (ApiException var11) {
                this.logFailureEvent(mapping, var11);
                return new Result(true);
            }

            return new Result(false);
        }
    }

    private boolean finalizerNotFound(V1SpringCloudGatewayMapping mapping) {
        return mapping.getMetadata().getFinalizers() == null || mapping.getMetadata().getFinalizers().isEmpty();
    }

    public boolean hasSynced() {
        return this.informer.hasSynced();
    }

    public boolean onUpdateFilter(V1SpringCloudGatewayMapping oldMapping, V1SpringCloudGatewayMapping newMapping) {
        if (newMapping.getMetadata().getDeletionTimestamp() != null) {
            return true;
        } else if (Objects.equals(oldMapping.getMetadata().getGeneration(), newMapping.getMetadata().getGeneration())) {
            return false;
        } else if (!Objects.equals(oldMapping.getSpec().getGatewayRef(), newMapping.getSpec().getGatewayRef())) {
            this.logFailureEvent(newMapping);
            return false;
        } else {
            return true;
        }
    }

    public boolean onDeleteFilter(V1SpringCloudGatewayMapping mapping, boolean deletedFinalStateUnknown) {
        return false;
    }

    private void logFailureEvent(V1SpringCloudGatewayMapping newMapping) {
        String message = String.format("Failed to process the update to mapping %s/%s as gatewayRef cannot be edited. Please revert the yaml to previous gatewayRef and apply.", newMapping.getSpec().getGatewayRef().getNamespace(), newMapping.getSpec().getGatewayRef().getName());
        LOG.error(message);
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(newMapping), (V1ObjectReference)null, "UnsupportedMappingUpdateException", message, EventType.Warning);
    }

    private void logFailureEvent(V1SpringCloudGatewayMapping mapping, V1Pod pod, PodUpdateException e) {
        String message = String.format("Failed to update pod %s with mapping %s. Ensure all filters/predicates are available and correctly setup", pod.getMetadata().getName(), mapping.getMetadata().getName());
        LOG.error(message, e);
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(mapping), ObjectReferenceConverter.toObjectReference(pod), "RouteUpdateException", message + ": " + e.getMessage(), EventType.Warning);
    }

    private void logFailureEvent(V1SpringCloudGatewayMapping mapping, UnprocessableRouteException e) {
        String message = String.format("Failed to retrieve routes from route config in mapping %s", mapping.getMetadata().getName());
        LOG.error(message, e);
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(mapping), (V1ObjectReference)null, "RoutesDefinitionException", message + ": " + e.getMessage(), EventType.Warning);
    }

    private void logFailureEvent(V1SpringCloudGatewayMapping mapping, ApiException e) {
        String message = String.format("Failed to edit finalizer for mapping %s", mapping.getMetadata().getName());
        LOG.error(message, e);
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(mapping), ObjectReferenceConverter.toObjectReference(mapping.getMetadata().getNamespace(), (String)null, "Finalizer"), "ApiException", message + ": " + e.getMessage(), EventType.Warning);
    }

    private void logSuccessEvent(V1SpringCloudGatewayMapping mapping, V1Pod pod, String reason) {
        String message = String.format("Routes specified in SpringCloudGatewayRouteConfig \"%s\" is %s on pod \"%s/%s\"", mapping.getSpec().getRouteConfigRef().getName(), reason, pod.getMetadata().getNamespace(), pod.getMetadata().getName());
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(mapping), ObjectReferenceConverter.toObjectReference(pod), reason, message, EventType.Normal);
    }

    private void logPodNotFoundEvent(Request request, V1SpringCloudGatewayMapping mapping) {
        String message = String.format("Specified SpringCloudGateway resource \"%s\" is not found / not ready", mapping.getSpec().getGatewayRef().getName());
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(mapping), ObjectReferenceConverter.toObjectReference(mapping.getMetadata().getNamespace(), mapping.getSpec().getGatewayRef().getNamespace(), mapping.getSpec().getGatewayRef().getName()), "NotFound", message, EventType.Warning);
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

