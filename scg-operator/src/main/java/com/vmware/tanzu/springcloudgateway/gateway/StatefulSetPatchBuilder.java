package com.vmware.tanzu.springcloudgateway.gateway;

import io.kubernetes.client.openapi.JSON;
import io.kubernetes.client.openapi.models.V1EnvVar;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import java.io.StringReader;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.json.Json;
import javax.json.JsonMergePatch;
import javax.json.JsonObject;
import javax.json.JsonPatchBuilder;
import javax.json.JsonPointer;
import org.springframework.stereotype.Component;

@Component
class StatefulSetPatchBuilder {
    private static final Set<String> STATEFUL_SET_IGNORED_FIELDS = Set.of("/status", "/spec/podManagementPolicy", "/spec/revisionHistoryLimit", "/spec/template/spec/dnsPolicy", "/spec/template/spec/containers/0/terminationMessagePath", "/spec/template/spec/containers/0/terminationMessagePolicy", "/spec/template/spec/schedulerName", "/spec/template/spec/serviceAccount", "/spec/volumeClaimTemplates/0/status", "/metadata/creationTimestamp", "/metadata/managedFields", "/metadata/resourceVersion", "/metadata/generation", "/metadata/selfLink", "/metadata/uid");
    private static final JSON json = new JSON();

    StatefulSetPatchBuilder() {
    }

    JsonMergePatch createJsonPatch(V1StatefulSet current, V1StatefulSet desired) {
        this.updateDesiredStateToAvoidBreakingChanges(current, desired);
        JsonObject currentJson = this.createJsonObject(this.sortEnvVars(current));
        JsonObject desiredJson = this.createJsonObject(this.sortEnvVars(desired));
        desiredJson = this.copyIgnoredFields(currentJson, desiredJson);
        return Json.createMergeDiff(currentJson, desiredJson);
    }

    private void updateDesiredStateToAvoidBreakingChanges(V1StatefulSet current, V1StatefulSet desired) {
        if (current.getSpec().getVolumeClaimTemplates() != null) {
            desired.getSpec().setVolumeClaimTemplates(current.getSpec().getVolumeClaimTemplates());
        }

    }

    private JsonObject createJsonObject(V1StatefulSet desired) {
        return Json.createReader(new StringReader(json.serialize(desired))).readObject();
    }

    private JsonObject copyIgnoredFields(JsonObject currentJson, JsonObject desiredJson) {
        JsonPatchBuilder patchBuilder = Json.createPatchBuilder();
        STATEFUL_SET_IGNORED_FIELDS.forEach((path) -> {
            JsonPointer pointer = Json.createPointer(path);
            if (pointer.containsValue(currentJson)) {
                if (pointer.containsValue(desiredJson)) {
                    patchBuilder.replace(path, pointer.getValue(currentJson));
                } else {
                    String parentPath = path.substring(0, path.lastIndexOf(47));
                    JsonPointer parentPointer = Json.createPointer(parentPath);
                    if (parentPointer.containsValue(desiredJson)) {
                        patchBuilder.add(path, pointer.getValue(currentJson));
                    }
                }
            }

        });
        return (JsonObject)patchBuilder.build().apply(desiredJson);
    }

    private V1StatefulSet sortEnvVars(V1StatefulSet statefulSet) {
        statefulSet.getSpec().getTemplate().getSpec().getContainers().forEach((container) -> {
            container.setEnv((List)container.getEnv().stream().sorted(Comparator.comparing(V1EnvVar::getName)).collect(Collectors.toList()));
        });
        return statefulSet;
    }
}

