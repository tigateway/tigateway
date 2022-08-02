package ti.gateway.operator.springcloudgateway.gateway;

import ti.gateway.operator.springcloudgateway.apis.LabelsBuilder;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import java.io.StringReader;
import java.util.Iterator;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonPatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
class ServiceBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpringCloudGatewayReconciler.class);
    static final String SERVICE_API_VERSION = "v1";
    static final String SERVICE_KIND = "Service";
    private static final String PORT_PROTOCOL = "TCP";
    private static final Set<String> HEADLESS_SERVICE_IGNORED_FIELDS = Set.of("/status", "/spec/sessionAffinity", "/spec/ipFamilies", "/spec/ipFamilyPolicy", "/spec/clusterIPs", "/metadata/creationTimestamp", "/metadata/managedFields", "/metadata/resourceVersion", "/metadata/selfLink", "/metadata/uid");
    private static final Set<String> SERVICE_IGNORED_FIELDS = Set.of("/status", "/spec/clusterIP", "/spec/clusterIPs", "/spec/ipFamilies", "/spec/ipFamilyPolicy", "/spec/sessionAffinity", "/metadata/creationTimestamp", "/metadata/managedFields", "/metadata/resourceVersion", "/metadata/selfLink", "/metadata/uid");
    private static final JSON json = new JSON();

    ServiceBuilder() {
    }

    V1Service createService(String namespace, V1SpringCloudGateway gateway) {
        String gatewayName = gateway.getMetadata().getName();
        return (new V1Service()).apiVersion("v1").kind("Service").metadata(serviceMetadata(namespace, gatewayName, gatewayName)).spec((new V1ServiceSpec()).type("ClusterIP").addPortsItem(gatewayServicePort(gateway)).selector(LabelsBuilder.buildServiceSelector(gateway.getMetadata().getName())));
    }

    V1Service createHeadlessService(String namespace, V1SpringCloudGateway gateway) {
        String gatewayName = gateway.getMetadata().getName();
        String serviceName = headlessServiceName(gatewayName);
        return (new V1Service()).apiVersion("v1").kind("Service").metadata(serviceMetadata(namespace, serviceName, gatewayName)).spec((new V1ServiceSpec()).type("ClusterIP").clusterIP("None").addPortsItem((new V1ServicePort()).port(5701).name("tcp-hazelcast").protocol("TCP").targetPort(new IntOrString(5701))).addPortsItem((new V1ServicePort()).port(8090).name("tcp-gateway-actuator").protocol("TCP").targetPort(new IntOrString(8090))).selector(LabelsBuilder.buildServiceSelector(gatewayName)));
    }

    static String headlessServiceName(String gatewayName) {
        return gatewayName + "-headless";
    }

    private static V1ObjectMeta serviceMetadata(String namespace, String serviceName, String gatewayName) {
        return (new V1ObjectMeta()).name(serviceName).namespace(namespace).labels(LabelsBuilder.build(gatewayName));
    }

    private static V1ServicePort gatewayServicePort(V1SpringCloudGateway gateway) {
        return isTlsEnabled(gateway) ? (new V1ServicePort()).port(443).protocol("TCP").name("https-gateway").targetPort(new IntOrString(8443)) : (new V1ServicePort()).port(80).protocol("TCP").name("http-gateway").targetPort(new IntOrString(8080));
    }

    private static boolean isTlsEnabled(V1SpringCloudGateway gateway) {
        return gateway.getSpec() != null && gateway.getSpec().getTls() != null && !gateway.getSpec().getTls().isEmpty();
    }

    JsonPatch buildServicesDiff(V1Service desired, V1Service current) {
        return this.buildServiceDiff(desired, current, SERVICE_IGNORED_FIELDS);
    }

    JsonPatch buildHeadlessServicesDiff(V1Service desired, V1Service current) {
        return this.buildServiceDiff(desired, current, HEADLESS_SERVICE_IGNORED_FIELDS);
    }

    private JsonPatch buildServiceDiff(V1Service desired, V1Service current, Set<String> ignoredFields) {
        JsonObject desiredJson = this.createJsonObject(desired);
        JsonObject currentJson = this.createJsonObject(current);
        JsonObject currentJsonPruned = this.removeIgnoredFields(currentJson, ignoredFields);
        return this.createDiff(desiredJson, currentJsonPruned);
    }

    private JsonPatch createDiff(JsonObject desiredJson, JsonObject currentJson) {
        return Json.createDiff(currentJson, desiredJson);
    }

    private JsonObject removeIgnoredFields(JsonObject currentJson, Set<String> ignoredFields) {
        Iterator var3 = ignoredFields.iterator();

        while(var3.hasNext()) {
            String field = (String)var3.next();

            try {
                currentJson = (JsonObject)Json.createPatchBuilder().remove(field).build().apply(currentJson);
            } catch (JsonException var6) {
                LOGGER.debug("Ignored field not found in current service: {}", field);
            }
        }

        return currentJson;
    }

    private JsonObject createJsonObject(V1Service desired) {
        return Json.createReader(new StringReader(json.serialize(desired))).readObject();
    }
}

