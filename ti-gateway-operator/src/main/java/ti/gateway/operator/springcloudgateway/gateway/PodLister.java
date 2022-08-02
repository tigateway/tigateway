package ti.gateway.operator.springcloudgateway.gateway;

import ti.gateway.operator.springcloudgateway.apis.LabelsBuilder;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PodLister {
    private final Logger LOG = LoggerFactory.getLogger(PodLister.class);
    private final CoreV1Api coreV1Api;

    public PodLister(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    public Set<V1Pod> listPodsWithContainersReady(String namespace, String name) {
        return this.filterPods(namespace, name, this::hasContainersReady);
    }

    private boolean hasContainersReady(V1Pod pod) {
        return pod.getStatus().getConditions().stream().filter((condition) -> {
            return condition.getType().equals("ContainersReady");
        }).allMatch((condition) -> {
            return "True".equals(condition.getStatus());
        });
    }

    public Collection<V1Pod> listReadyPods(String namespace, String gatewayName) {
        return this.filterPods(namespace, gatewayName, PodLister::podIsReady);
    }

    public Set<V1Pod> filterPods(String namespace, String gatewayName, Predicate<V1Pod> predicate) {
        return (Set)this.getAllPods(namespace, gatewayName).filter(predicate).collect(Collectors.toSet());
    }

    private Stream<V1Pod> getAllPods(String namespace, String gatewayName) {
        try {
            String selector = LabelsBuilder.gatewayChildResourceSelector(gatewayName);
            V1PodList list = this.coreV1Api.listNamespacedPod(namespace, (String)null, (Boolean)null, (String)null, (String)null, selector, (Integer)null, (String)null, (String)null, (Integer)null, (Boolean)null);
            return list.getItems().stream();
        } catch (ApiException var5) {
            this.LOG.error("Failed to list running pods: " + var5.getResponseBody(), var5);
            throw new RuntimeException(var5);
        }
    }

    static boolean podIsReady(V1Pod pod) {
        return "Running".equals(pod.getStatus().getPhase()) && pod.getStatus().getConditions().stream().allMatch((v1PodCondition) -> {
            return "True".equals(v1PodCondition.getStatus());
        });
    }
}

