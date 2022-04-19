package com.vmware.tanzu.springcloudgateway.apis;

import java.util.HashMap;
import java.util.Map;

public class LabelsBuilder {
    public static final String SCG_PART_OF_LABEL_KEY = "app.kubernetes.io/part-of";
    public static final String SCG_SERVICE_SELECTOR_LABEL_KEY = "app";
    public static final String SCG_OPERATOR_APP_LABEL = "scg-operator";
    public static final String SCG_CHILD_RESOURCE_LABEL_KEY = "gateway.name";
    public static final String SCG_CHILD_RESOURCE_PART_OF_LABEL = "spring-cloud-gateway";

    public LabelsBuilder() {
    }

    public static Map<String, String> build(String gatewayName) {
        Map<String, String> labels = new HashMap<>();
        labels.put("gateway.name", gatewayName);
        labels.put("app.kubernetes.io/part-of", "spring-cloud-gateway");
        labels.put("app", gatewayName);
        return labels;
    }

    public static Map<String, String> buildServiceSelector(String gatewayName) {
        Map<String, String> labels = new HashMap<>();
        labels.put("app", gatewayName);
        return labels;
    }

    public static String gatewayChildResourceSelector(String gatewayName) {
        return String.format("%s=%s", "gateway.name", gatewayName);
    }

    public static String gatewayAppLabel() {
        return String.format("%s=%s", "app.kubernetes.io/part-of", "spring-cloud-gateway");
    }

    public static String operatorAppLabel() {
        return String.format("%s=%s", "app", "scg-operator");
    }
}
