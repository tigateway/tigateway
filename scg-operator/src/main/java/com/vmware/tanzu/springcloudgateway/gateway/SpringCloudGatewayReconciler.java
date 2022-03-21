package com.vmware.tanzu.springcloudgateway.gateway;

import com.vmware.tanzu.springcloudgateway.apis.EventRecorder;
import com.vmware.tanzu.springcloudgateway.apis.ObjectReferenceConverter;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.extended.controller.reconciler.Reconciler;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.controller.reconciler.Result;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.informer.cache.Lister;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1ContainerStatus;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1ObjectReference;
import io.kubernetes.client.openapi.models.V1OwnerReference;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1Secret;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import java.util.List;
import java.util.function.Predicate;
import javax.json.JsonPatch;
import javax.json.JsonValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class SpringCloudGatewayReconciler implements Reconciler {
    public static final String CONTROLLER_NAME = "SpringCloudGatewayController";
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudGatewayReconciler.class);
    private final AppsV1Api appsV1Api;
    private final Lister<V1Pod> podLister;
    private final CoreV1Api coreV1Api;
    private final Lister<V1StatefulSet> statefulSetLister;
    private final Lister<V1SpringCloudGateway> gatewayLister;
    private final RbacBuilder rbacBuilder;
    private final Lister<V1Service> serviceLister;
    private final StatefulSetBuilder statefulSetBuilder;
    private final StatefulSetPatchBuilder statefulSetPatchBuilder;
    private final ServiceBuilder serviceBuilder;
    private final EventRecorder eventRecorder;
    private final GatewayStatusEditor gatewayStatusEditor;
    private final OperatorProperties operatorProperties;
    private final ServiceMonitorReconciler serviceMonitorReconciler;

    public SpringCloudGatewayReconciler(Lister<V1SpringCloudGateway> gatewayLister, Lister<V1StatefulSet> statefulSetLister, StatefulSetBuilder statefulSetBuilder, StatefulSetPatchBuilder statefulSetPatchBuilder, ServiceBuilder serviceBuilder, RbacBuilder rbacBuilder, Lister<V1Service> serviceIndexInformer, Lister<V1Pod> podLister, CoreV1Api coreV1Api, AppsV1Api appsV1Api, EventRecorder eventRecorder, GatewayStatusEditor gatewayStatusEditor, OperatorProperties operatorProperties, ServiceMonitorReconciler serviceMonitorReconciler) {
        this.gatewayLister = gatewayLister;
        this.statefulSetLister = statefulSetLister;
        this.statefulSetBuilder = statefulSetBuilder;
        this.statefulSetPatchBuilder = statefulSetPatchBuilder;
        this.serviceBuilder = serviceBuilder;
        this.rbacBuilder = rbacBuilder;
        this.serviceLister = serviceIndexInformer;
        this.podLister = podLister;
        this.coreV1Api = coreV1Api;
        this.appsV1Api = appsV1Api;
        this.eventRecorder = eventRecorder;
        this.gatewayStatusEditor = gatewayStatusEditor;
        this.operatorProperties = operatorProperties;
        this.serviceMonitorReconciler = serviceMonitorReconciler;
    }

    public boolean onDeleteFilter(V1SpringCloudGateway springCloudGateway, Boolean cacheStatusUnknown) {
        return false;
    }

    public boolean onUpdateFilter(V1SpringCloudGateway oldGateway, V1SpringCloudGateway newGateway) {
        return !oldGateway.getMetadata().getGeneration().equals(newGateway.getMetadata().getGeneration());
    }

    public Result reconcile(Request request) {
        String gatewayNamespace = request.getNamespace();
        String gatewayName = request.getName();
        V1SpringCloudGateway desiredGateway = (V1SpringCloudGateway)this.gatewayLister.namespace(gatewayNamespace).get(gatewayName);
        LOGGER.debug("Desired V1SpringCloudGateway: {}", desiredGateway);

        try {
            if (desiredGateway != null) {
                this.reconcileImagePullSecret(gatewayNamespace, desiredGateway);
                this.reconcileServices(desiredGateway, gatewayNamespace);
                this.serviceMonitorReconciler.reconcile(desiredGateway, gatewayNamespace, createOwnerReference(desiredGateway));
                V1StatefulSet statefulSet = this.reconcileStatefulSet(desiredGateway, gatewayNamespace);
                return this.setGatewayStatus(desiredGateway, gatewayNamespace, statefulSet);
            } else {
                return new Result(false);
            }
        } catch (ApiException var6) {
            this.setGatewayFailed(desiredGateway, var6);
            return new Result(true);
        }
    }

    private void reconcileImagePullSecret(String gatewayNamespace, V1SpringCloudGateway desiredGateway) throws ApiException {
        String installationNamespace = this.operatorProperties.getInstallNamespace();
        String secretName = this.operatorProperties.getImagePullSecretName();
        LOGGER.debug("{} / {} / {}", new Object[]{this.operatorProperties.getGatewayImageName(), this.operatorProperties.getImagePullSecretName(), this.operatorProperties.getInstallNamespace()});

        try {
            V1Secret imagePullSecret = this.coreV1Api.readNamespacedSecret(secretName, installationNamespace, (String)null, (Boolean)null, (Boolean)null);
            imagePullSecret.getMetadata().setResourceVersion((String)null);
            imagePullSecret.getMetadata().setNamespace(gatewayNamespace);
            imagePullSecret.getMetadata().addOwnerReferencesItem(createOwnerReference(desiredGateway));

            try {
                this.coreV1Api.createNamespacedSecret(gatewayNamespace, imagePullSecret, (String)null, (String)null, (String)null);
                this.logSuccessEvent(desiredGateway, imagePullSecret, "Created");
            } catch (ApiException var7) {
                if (var7.getCode() != HttpStatus.CONFLICT.value()) {
                    throw var7;
                }

                LOGGER.debug("secret {} already exists in namespace {}", secretName, gatewayNamespace);
            }
        } catch (ApiException var8) {
            if (var8.getCode() != HttpStatus.NOT_FOUND.value()) {
                throw var8;
            }

            LOGGER.debug("secret {} does not exists in namespace {}", secretName, gatewayNamespace);
        }

    }

    private V1StatefulSet reconcileStatefulSet(V1SpringCloudGateway desiredGateway, String desiredNamespace) throws ApiException {
        String statefulSetName = desiredGateway.getMetadata().getName();
        V1StatefulSet desiredStatefulSet = this.statefulSetBuilder.createStatefulSet(desiredNamespace, desiredGateway);
        V1StatefulSet currentStatefulSet = (V1StatefulSet)this.statefulSetLister.namespace(desiredNamespace).get(statefulSetName);
        desiredStatefulSet.getMetadata().addOwnerReferencesItem(createOwnerReference(desiredGateway));
        if (currentStatefulSet == null) {
            this.rbacBuilder.create(desiredGateway, desiredNamespace);
            return this.createStatefulSet(desiredNamespace, statefulSetName, desiredStatefulSet);
        } else {
            if (currentStatefulSet.getApiVersion() == null) {
                currentStatefulSet.setApiVersion("apps/v1");
                currentStatefulSet.setKind("StatefulSet");
            }

            JsonValue statefulSetPatch = this.statefulSetPatchBuilder.createJsonPatch(currentStatefulSet, desiredStatefulSet).toJsonValue();
            if (!statefulSetPatch.asJsonObject().isEmpty()) {
                this.rbacBuilder.update(desiredGateway, desiredNamespace, currentStatefulSet.getSpec().getTemplate().getSpec().getServiceAccountName());
                return this.updateStatefulSet(desiredNamespace, statefulSetName, desiredStatefulSet, statefulSetPatch);
            } else {
                return currentStatefulSet;
            }
        }
    }

    private V1StatefulSet updateStatefulSet(String desiredNamespace, String statefulSetName, V1StatefulSet desiredStatefulSet, JsonValue statefulSetPatch) throws ApiException {
        try {
            return this.statefulSetBuilder.patchStatefulSet(desiredNamespace, statefulSetName, statefulSetPatch.toString());
        } catch (ApiException var6) {
            if (var6.getCode() == HttpStatus.UNPROCESSABLE_ENTITY.value()) {
                LOGGER.info("Cannot update old statefulset, deleting old one and creating a new one");
                this.appsV1Api.deleteNamespacedStatefulSet(statefulSetName, desiredNamespace, (String)null, (String)null, 0, (Boolean)null, "Background", (V1DeleteOptions)null);
                return this.createStatefulSet(desiredNamespace, statefulSetName, desiredStatefulSet);
            } else {
                throw var6;
            }
        }
    }

    private void reconcileServices(V1SpringCloudGateway desiredGateway, String desiredNamespace) throws ApiException {
        V1Service desiredService = this.serviceBuilder.createService(desiredNamespace, desiredGateway);
        String serviceName = desiredGateway.getMetadata().getName();
        String headlessServiceName = ServiceBuilder.headlessServiceName(serviceName);
        V1Service currentService = (V1Service)this.serviceLister.namespace(desiredNamespace).get(serviceName);
        V1Service currentHeadlessService = (V1Service)this.serviceLister.namespace(desiredNamespace).get(headlessServiceName);
        V1Service desiredHeadlessService = this.serviceBuilder.createHeadlessService(desiredNamespace, desiredGateway);
        desiredService.getMetadata().addOwnerReferencesItem(createOwnerReference(desiredGateway));
        desiredHeadlessService.getMetadata().addOwnerReferencesItem(createOwnerReference(desiredGateway));
        JsonPatch headlessServicesDiff;
        if (currentService == null) {
            this.createService(desiredNamespace, desiredService);
        } else {
            if (currentService.getApiVersion() == null) {
                currentService.setApiVersion("v1");
                currentService.kind("Service");
            }

            headlessServicesDiff = this.serviceBuilder.buildServicesDiff(desiredService, currentService);
            if (!headlessServicesDiff.toJsonArray().isEmpty()) {
                this.patchService(desiredNamespace, serviceName, headlessServicesDiff);
            }
        }

        if (currentHeadlessService == null) {
            this.createService(desiredHeadlessService.getMetadata().getNamespace(), desiredHeadlessService);
        } else {
            if (currentHeadlessService.getApiVersion() == null) {
                currentHeadlessService.setApiVersion("v1");
                currentHeadlessService.kind("Service");
            }

            headlessServicesDiff = this.serviceBuilder.buildHeadlessServicesDiff(desiredHeadlessService, currentHeadlessService);
            if (!headlessServicesDiff.toJsonArray().isEmpty()) {
                this.patchService(desiredNamespace, headlessServiceName, headlessServicesDiff);
            }
        }

    }

    private Result setGatewayStatus(V1SpringCloudGateway desiredGateway, String gatewayNamespace, V1StatefulSet currentStatefulSet) {
        if (this.gatewayPodsReady(desiredGateway, currentStatefulSet, gatewayNamespace)) {
            this.setGatewayReady(desiredGateway, currentStatefulSet);
            return new Result(false);
        } else {
            this.gatewayStatusEditor.setGatewayStatus(desiredGateway, "Ready", "False", "WaitingPods");
            return new Result(true);
        }
    }

    private V1StatefulSet createStatefulSet(String desiredNamespace, String statefulSetName, V1StatefulSet desiredStatefulSet) throws ApiException {
        LOGGER.info("Existing StatefulSet {}/{} not found, creating new one", desiredNamespace, statefulSetName);
        return this.appsV1Api.createNamespacedStatefulSet(desiredNamespace, desiredStatefulSet, (String)null, (String)null, (String)null);
    }

    private boolean gatewayPodsReady(V1SpringCloudGateway desiredGateway, V1StatefulSet currentStatefulSet, String namespace) {
        if (currentStatefulSet == null) {
            return false;
        } else {
            boolean countNotSpecified = desiredGateway.getSpec() == null || desiredGateway.getSpec().getCount() == null;
            int desiredCount = countNotSpecified ? 1 : desiredGateway.getSpec().getCount();
            long readyPodCount = this.podLister.namespace(namespace).list().stream().filter(this.podIsOwnedByGateway(desiredGateway)).filter((pod) -> {
                return pod.getStatus() != null;
            }).filter(this.podIsManagedByStatefulSet(currentStatefulSet)).filter(this.podContainersReady()).count();
            return readyPodCount == (long)desiredCount;
        }
    }

    private Predicate<V1Pod> podIsOwnedByGateway(V1SpringCloudGateway desiredGateway) {
        return (pod) -> {
            return ((String)pod.getMetadata().getLabels().get("gateway.name")).equals(desiredGateway.getMetadata().getName());
        };
    }

    private Predicate<V1Pod> podContainersReady() {
        return (pod) -> {
            List<V1ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
            return containerStatuses != null && containerStatuses.stream().allMatch(V1ContainerStatus::getReady);
        };
    }

    private Predicate<V1Pod> podIsManagedByStatefulSet(V1StatefulSet currentStatefulSet) {
        return (pod) -> {
            Long generation = currentStatefulSet.getMetadata().getGeneration();
            Long observedGeneration = currentStatefulSet.getStatus().getObservedGeneration();
            if (!generation.equals(observedGeneration)) {
                return false;
            } else {
                return generation > 1L ? ((String)pod.getMetadata().getLabels().get("controller-revision-hash")).equals(currentStatefulSet.getStatus().getUpdateRevision()) : true;
            }
        };
    }

    private void setGatewayReady(V1SpringCloudGateway gateway, V1StatefulSet currentStatefulSet) {
        String reason;
        if (currentStatefulSet.getStatus().getObservedGeneration() == 1L && gateway.getMetadata().getGeneration() == 1L) {
            reason = "Created";
        } else {
            reason = "Updated";
        }

        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(gateway), (V1ObjectReference)null, reason, String.format("SpringCloudGateway resource %s is %s", gateway.getMetadata().getName(), reason), EventType.Normal);
        this.gatewayStatusEditor.setGatewayStatus(gateway, "Ready", "True", reason);
    }

    private void setGatewayFailed(V1SpringCloudGateway gateway, ApiException e) {
        String errorMessage = e.getMessage();
        if (e.getResponseBody() != null) {
            errorMessage = errorMessage + ", response is " + e.getResponseBody();
        }

        LOGGER.error(errorMessage, e);
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(gateway), (V1ObjectReference)null, "ApiException", errorMessage, EventType.Warning);
        this.gatewayStatusEditor.setGatewayStatus(gateway, "Ready", "False", "ApiExceptionWithCode" + e.getCode());
    }

    private void createService(String desiredNamespace, V1Service desiredService) throws ApiException {
        this.coreV1Api.createNamespacedService(desiredNamespace, desiredService, (String)null, (String)null, (String)null);
        LOGGER.info("Created Service {}/{}", desiredService.getMetadata().getNamespace(), desiredService.getMetadata().getName());
    }

    private void patchService(String desiredNamespace, String serviceName, JsonPatch servicesDiff) throws ApiException {
        LOGGER.info("Desired service {} is different from current. Applying patch {}", serviceName, servicesDiff.toJsonArray().toString());
        this.coreV1Api.patchNamespacedService(serviceName, desiredNamespace, new V1Patch(servicesDiff.toJsonArray().toString()), (String)null, (String)null, (String)null, (Boolean)null);
    }

    static V1OwnerReference createOwnerReference(V1SpringCloudGateway owner) {
        return (new V1OwnerReference()).controller(true).name(owner.getMetadata().getName()).uid(owner.getMetadata().getUid()).kind(owner.getKind()).apiVersion(owner.getApiVersion()).blockOwnerDeletion(true);
    }

    private void logSuccessEvent(V1SpringCloudGateway gateway, V1Secret secret, String reason) {
        String message = String.format("ImagePullSecret specified in SpringCloudGatewayRouteConfig \"%s\" is %s on pod \"%s/%s\"", gateway.getMetadata().getName(), reason, secret.getMetadata().getNamespace(), secret.getMetadata().getName());
        this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(gateway), ObjectReferenceConverter.toObjectReference(secret), reason, message, EventType.Normal);
    }
}

