package ti.gateway.operator.springcloudgateway.apis;

import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.CoreV1Event;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1ObjectReference;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.OffsetDateTime;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;

public class EventRecorder {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventRecorder.class);
    private static final String ACTION = "Reconcile";
    private static final String REPORTING_CONTROLLER = "SpringCloudGatewayController";
    private final CoreV1Api coreV1Api;

    public EventRecorder(CoreV1Api coreV1Api) {
        this.coreV1Api = coreV1Api;
    }

    public void logEvent(@NotNull V1ObjectReference regardingObjectRef, @Nullable V1ObjectReference relatedObjectRef, String reason, String message, EventType type) {
        try {
            CoreV1Event event = (new CoreV1Event()).metadata((new V1ObjectMeta()).generateName(regardingObjectRef.getName() + "-")).type(type.toString()).action("Reconcile").reason(reason).message(message).related(relatedObjectRef).involvedObject(regardingObjectRef).firstTimestamp(OffsetDateTime.now()).lastTimestamp(OffsetDateTime.now()).reportingInstance(InetAddress.getLocalHost().getHostName()).reportingComponent("SpringCloudGatewayController");
            this.coreV1Api.createNamespacedEvent(regardingObjectRef.getNamespace(), event, (String)null, (String)null, (String)null);
        } catch (ApiException var7) {
            if (var7.getCode() == 404) {
                LOGGER.error("Unable to create events using events.k8s.io/v1beta1 API");
                return;
            }

            if (var7.getResponseBody() != null) {
                message = message + ". Event creation failed due to: " + var7.getResponseBody();
            }

            LOGGER.error(message + var7.getResponseBody(), var7);
        } catch (UnknownHostException var8) {
            LOGGER.error("Unable to create events using events.k8s.io/v1beta1 API", var8);
        }

    }

    public void recordGatewayEvent(String namespace, V1SpringCloudGateway gateway, String message) {
        try {
            this.coreV1Api.createNamespacedEvent(namespace, (new CoreV1Event()).kind(gateway.getKind()).apiVersion(gateway.getApiVersion()).action("Configuring SpringCloudGateway").message(message), (String)null, (String)null, (String)null);
        } catch (ApiException var5) {
            LOGGER.error(message, var5);
        }

    }
}

