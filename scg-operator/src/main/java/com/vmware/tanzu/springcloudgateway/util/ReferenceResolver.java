package com.vmware.tanzu.springcloudgateway.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewayRouteConfigSpecModel;
import com.vmware.tanzu.springcloudgateway.models.V1SpringCloudGatewayRouteConfigSpecRoutes;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.commons.lang3.StringUtils;

public class ReferenceResolver {
    private static final int MAX_REFS = 10;
    private final ObjectMapper mapper;
    private final JsonNode source;

    public ReferenceResolver(Object source) {
        if (source == null) {
            throw new ReferenceResolverException("Source must not be null");
        } else {
            this.mapper = new ObjectMapper();
            this.source = this.mapper.valueToTree(source);
        }
    }

    public V1SpringCloudGatewayRouteConfigSpecRoutes resolveRoute(V1SpringCloudGatewayRouteConfigSpecRoutes route) {
        V1SpringCloudGatewayRouteConfigSpecModel model = route.getModel();
        if (model != null) {
            Object requestBody = model.getRequestBody();
            if (requestBody != null) {
                requestBody = this.resolveMap(this.convertValue(requestBody), new AtomicInteger());
                model.setRequestBody(requestBody);
            }

            Object responses = model.getResponses();
            if (responses != null) {
                responses = this.resolveMap(this.convertValue(responses), new AtomicInteger());
                model.setResponses(responses);
            }
        }

        return route;
    }

    private Map<String, Object> resolveMap(Map<String, Object> map, AtomicInteger refs) {
        Iterator<Entry<String, Object>> var3 = (new HashMap<>(map)).entrySet().iterator();

        while(var3.hasNext()) {
            Entry<String, Object> entry = (Entry<String, Object>)var3.next();
            String k = (String)entry.getKey();
            Object v = entry.getValue();
            if ("$ref".equals(k)) {
                refs.incrementAndGet();
                Map<String, Object> value = this.resolveExpression((String)v, refs);
                map.putAll(value);
                map.remove(k);
            } else if (v instanceof Map) {
                this.resolveMap((Map<String, Object>)v, refs);
            }
        }

        return new ConcurrentHashMap<>(map);
    }

    Map<String, Object> resolveExpression(String ref, AtomicInteger refs) {
        if (refs.get() > 10) {
            throw new ReferenceResolverException("Max refs reached");
        } else if (StringUtils.isBlank(ref)) {
            throw new ReferenceResolverException("Reference must not be blank");
        } else {
            if (ref.startsWith("#")) {
                ref = ref.replaceFirst("#", "");
            }

            if (!ref.startsWith("/")) {
                ref = "/" + ref;
            }

            JsonNode n = this.source.at(ref);
            if (n.isMissingNode()) {
                throw new ReferenceResolverException("Reference not found: " + ref);
            } else {
                Map<String, Object> v = (Map)this.convertValue(n);
                return new ConcurrentHashMap(this.resolveMap(v, refs));
            }
        }
    }

    <T> T convertValue(Object obj) {
        return this.mapper.convertValue(obj, new TypeReference<T>() {
        });
    }
}

