package ti.gateway.operator.springcloudgateway.route;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayRouteConfigSpecRoutes;
import io.kubernetes.client.openapi.models.V1Secret;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;

public class RouteDefinition {
    private static final String CONFLICTING_CONFIGURATION_ERROR_MESSAGE_PREFIX = "Conflicting configuration for key";
    private static final String PATH_PREDICATE_KEY = "Path";
    private static final String METHOD_PREDICATE_KEY = "Method";
    private static final String RATE_LIMIT_FILTER_KEY = "RateLimit";
    private static final String STRIP_PREFIX_FILTER_KEY = "StripPrefix";
    private static final String BASIC_AUTH_FILTER_KEY = "BasicAuth";
    private static final String DESCRIPTION_METADATA_KEY = "description";
    private static final String DOCUMENTATION_METADATA_KEY = "documentation";
    private static final String TITLE_METADATA_KEY = "title";
    private static final String REQUEST_BODY_METADATA_KEY = "requestBody";
    private static final String RESPONSES_METADATA_KEY = "responses";
    private static final String TAGS_METADATA_KEY = "tags";
    private static final String SSO_LOGIN_FILTER = "SsoLogin";
    private static final String TOKEN_RELAY_FILTER = "TokenRelay";
    private static final String BASIC_AUTH_USERNAME_KEY = "username";
    private static final String BASIC_AUTH_PASSWORD_KEY = "password";
    private static final Set<String> DENY_LIST = Set.of("BasicAuth", "TokenRelay", "SsoLogin");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private String id;
    private String uri;
    private List<String> predicates;
    private List<String> filters;
    private int order;
    private Map<String, Object> metadata;

    static RouteDefinition from(V1SpringCloudGatewayRouteConfigSpecRoutes crdRoute) {
        return from(crdRoute, (V1Secret)null);
    }

    static RouteDefinition from(V1SpringCloudGatewayRouteConfigSpecRoutes crdRoute, V1Secret basicAuthSecret) {
        if (crdRoute == null) {
            return null;
        } else {
            RouteDefinition.Builder builder = builder().uri(crdRoute.getUri()).predicates(crdRoute.getPredicates()).filters(validFilters(crdRoute.getFilters()));
            if (StringUtils.hasText(crdRoute.getTitle())) {
                builder.title(crdRoute.getTitle());
            }

            if (StringUtils.hasText(crdRoute.getDescription())) {
                builder.description(crdRoute.getDescription());
            }

            if (crdRoute.getSsoEnabled() != null) {
                builder.ssoEnabled(crdRoute.getSsoEnabled());
            }

            if (crdRoute.getTokenRelay() != null) {
                builder.tokenRelay(crdRoute.getTokenRelay());
            }

            if (crdRoute.getOrder() != null) {
                builder.order(crdRoute.getOrder());
            }

            if (crdRoute.getTags() != null) {
                builder.tags(Set.copyOf(crdRoute.getTags()));
            }

            if (crdRoute.getModel() != null) {
                builder.requestBody(crdRoute.getModel().getRequestBody());
                builder.responses(crdRoute.getModel().getResponses());
            }

            if (basicAuthSecret != null && basicAuthSecret.getData() != null && basicAuthSecret.getData().containsKey("username") && basicAuthSecret.getData().containsKey("password") && crdRoute.getFilters() != null && crdRoute.getFilters().contains("BasicAuth")) {
                byte[] decodedUsername = (byte[])basicAuthSecret.getData().get("username");
                byte[] decodedPassword = (byte[])basicAuthSecret.getData().get("password");
                builder.basicAuth(new String(decodedUsername), new String(decodedPassword));
            }

            return builder.build();
        }
    }

    private static List<String> validFilters(List<String> filters) {
        return filters != null ? (List)filters.stream().filter((filter) -> {
            return !DENY_LIST.contains(filter);
        }).collect(Collectors.toList()) : List.of();
    }

    public String toString() {
        return "class RouteDefinition {\n    id: " + this.id + "\n    uri: " + this.uri + "\n    predicates: " + this.predicates + "\n    filters: " + this.filters + "\n    order: " + this.order + "\n    metadata: " + this.metadata + "\n}";
    }

    public RouteDefinition(String routeId, String uri, List<String> predicates, List<String> filters, int order, Map<String, Object> metadata) {
        this.id = routeId;
        this.uri = uri;
        this.predicates = predicates;
        this.filters = filters;
        this.order = order;
        this.metadata = metadata;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUri() {
        return this.uri;
    }

    public int getOrder() {
        return this.order;
    }

    public List<String> getFilters() {
        return Collections.unmodifiableList(this.filters);
    }

    public String getPredicateValueFor(String key) {
        return (String)convertKeyValueListToMap(this.predicates).get(key);
    }

    public List<String> getPredicates() {
        return this.predicates;
    }

    public String getPath() {
        return this.getPredicateValueFor("Path");
    }

    public String getMethod() {
        return this.getPredicateValueFor("Method");
    }

    public Set<String> getTags() {
        return (Set)this.metadata.get("tags");
    }

    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(this.metadata);
    }

    public String getDescription() {
        return (String)this.metadata.get("description");
    }

    public String getTitle() {
        return (String)this.metadata.get("title");
    }

    public String getDocumentation() {
        return (String)this.metadata.get("documentation");
    }

    public RequestBody getRequestBody() {
        return (RequestBody)this.metadata.get("requestBody");
    }

    public ApiResponses getResponses() {
        return (ApiResponses)this.metadata.get("responses");
    }

    public boolean hasSsoLoginFilter() {
        return this.getFilters().contains("SsoLogin");
    }

    public static RouteDefinition.Builder builder() {
        return new RouteDefinition.Builder();
    }

    private static String convertToKeyValueString(String key, String value) {
        return key + "=" + value;
    }

    private static Map<String, String> convertKeyValueListToMap(Collection<String> keyValueList) {
        Map<String, String> map = new LinkedHashMap();
        if (keyValueList != null) {
            keyValueList.stream().filter(Objects::nonNull).forEach((kvString) -> {
                String[] kvArray = kvString.split("=", 2);
                map.put(kvArray[0], kvArray.length > 1 ? kvArray[1] : null);
            });
        }

        return map;
    }

    private static class CustomApiResponsesDeserializer extends StdDeserializer<ApiResponses> {
        protected CustomApiResponsesDeserializer() {
            this((Class)null);
        }

        protected CustomApiResponsesDeserializer(Class<?> vc) {
            super(vc);
        }

        public ApiResponses deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            JsonNode node = (JsonNode)p.getCodec().readTree(p);
            ApiResponses responses = new ApiResponses();
            Iterator fields = node.fields();

            while(fields.hasNext()) {
                Entry<String, JsonNode> next = (Entry)fields.next();
                ApiResponse apiResponse = (ApiResponse)Json.mapper().convertValue(next.getValue(), ApiResponse.class);
                if (this.isHttpStatusCodeValid((String)next.getKey())) {
                    responses.addApiResponse((String)next.getKey(), apiResponse);
                }
            }

            return responses;
        }

        private boolean isHttpStatusCodeValid(String statusCode) {
            Pattern p = Pattern.compile("^([1-5](XX|[0-9]{2})|default)$");
            return p.matcher(statusCode).matches();
        }
    }

    public static class Builder {
        private String routeId;
        private String uri;
        private int order;
        private String path;
        private String method;
        private boolean ssoEnabled;
        private List<String> predicates = new ArrayList<>();
        private List<String> filters = new ArrayList<>();
        private String rateLimit;
        private Map<String, Object> metadata = new HashMap<>();
        private String description;
        private String documentation;
        private String title;
        private boolean tokenRelay;
        private String basicAuth;
        private Set<String> tags = new HashSet<>();
        private Object requestBodyObj;
        private Object responses;

        public Builder() {
        }

        public RouteDefinition.Builder routeId(String routeId) {
            this.routeId = routeId;
            return this;
        }

        public RouteDefinition.Builder tags(Set<String> tags) {
            if (tags != null) {
                this.tags.addAll((Collection)tags.stream().filter(StringUtils::hasText).collect(Collectors.toList()));
            }

            return this;
        }

        public RouteDefinition.Builder uri(String uri) {
            this.uri = uri;
            return this;
        }

        public RouteDefinition.Builder order(int order) {
            this.order = order;
            return this;
        }

        public RouteDefinition.Builder predicate(String predicate) {
            if (predicate != null) {
                this.predicates.add(predicate);
            }

            return this;
        }

        public RouteDefinition.Builder predicates(List<String> predicates) {
            if (predicates != null) {
                this.predicates.addAll(predicates);
            }

            return this;
        }

        public RouteDefinition.Builder filter(String filter) {
            if (filter != null) {
                this.filters.add(filter);
            }

            return this;
        }

        public RouteDefinition.Builder filters(List<String> filters) {
            if (filters != null) {
                this.filters.addAll(filters);
            }

            return this;
        }

        public RouteDefinition.Builder ssoEnabled(boolean ssoEnabled) {
            this.ssoEnabled = ssoEnabled;
            return this;
        }

        public RouteDefinition.Builder tokenRelay(boolean tokenRelay) {
            this.tokenRelay = tokenRelay;
            return this;
        }

        public RouteDefinition.Builder basicAuth(String decodedUsername, String decodedPassword) {
            this.basicAuth = Base64Utils.encodeToString((decodedUsername + ":" + decodedPassword).getBytes());
            return this;
        }

        public RouteDefinition.Builder path(String path) {
            this.path = path;
            return this;
        }

        public RouteDefinition.Builder method(String method) {
            this.method = method;
            return this;
        }

        public RouteDefinition.Builder rateLimit(String rateLimit) {
            this.rateLimit = rateLimit;
            return this;
        }

        public RouteDefinition.Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public RouteDefinition.Builder description(String description) {
            this.description = description;
            return this;
        }

        public RouteDefinition.Builder documentation(String documentation) {
            this.documentation = documentation;
            return this;
        }

        public RouteDefinition.Builder title(String title) {
            this.title = title;
            return this;
        }

        public RouteDefinition.Builder requestBody(Object requestBody) {
            this.requestBodyObj = requestBody;
            return this;
        }

        public RouteDefinition.Builder responses(Object responses) {
            this.responses = responses;
            return this;
        }

        public RouteDefinition build() {
            RouteDefinition routeDefinition = new RouteDefinition(this.routeId, this.uri, this.predicates, this.filters, this.order, this.metadata);
            this.buildFilters(routeDefinition);
            this.buildPredicates(routeDefinition);
            this.buildMetadata(routeDefinition);
            return routeDefinition;
        }

        private void buildMetadata(RouteDefinition routeDefinition) {
            if (StringUtils.hasText(this.description)) {
                if (this.metadata.containsKey("description")) {
                    throw new RouteDefinitionException("Conflicting configuration for key: description");
                }

                routeDefinition.metadata.put("description", this.description);
            }

            if (StringUtils.hasText(this.documentation)) {
                if (this.metadata.containsKey("documentation")) {
                    throw new RouteDefinitionException("Conflicting configuration for key: documentation");
                }

                routeDefinition.metadata.put("documentation", this.documentation);
            }

            if (StringUtils.hasText(this.title)) {
                if (this.metadata.containsKey("title")) {
                    throw new RouteDefinitionException("Conflicting configuration for key: title");
                }

                routeDefinition.metadata.put("title", this.title);
            }

            if (!this.tags.isEmpty()) {
                if (this.metadata.containsKey("tags")) {
                    throw new RouteDefinitionException("Conflicting configuration for key: tags");
                }

                routeDefinition.metadata.put("tags", Collections.unmodifiableSet(this.tags));
            }

            if (this.requestBodyObj != null) {
                if (this.metadata.containsKey("requestBody")) {
                    throw new RouteDefinitionException("Conflicting configuration for key: requestBody");
                }

                try {
                    RequestBody requestBody;
                    if (this.requestBodyObj instanceof RequestBody) {
                        requestBody = (RequestBody)this.requestBodyObj;
                    } else {
                        requestBody = (RequestBody)Json.mapper().convertValue(this.requestBodyObj, RequestBody.class);
                    }

                    routeDefinition.metadata.put("requestBody", requestBody);
                } catch (IllegalArgumentException var5) {
                    throw new RouteDefinitionException("Unable to convert " + this.requestBodyObj.getClass() + " instance to " + RequestBody.class + " instance. Details: " + var5.getMessage(), var5);
                }
            }

            if (this.responses != null) {
                if (this.metadata.containsKey("responses")) {
                    throw new RouteDefinitionException("Conflicting configuration for key: responses");
                }

                try {
                    SimpleModule module = (new SimpleModule()).addDeserializer(ApiResponses.class, new RouteDefinition.CustomApiResponsesDeserializer());
                    ApiResponses apiResponses = (ApiResponses)RouteDefinition.MAPPER.registerModule(module).convertValue(this.responses, ApiResponses.class);
                    apiResponses.values().stream().filter((response) -> {
                        return !StringUtils.hasText(response.getDescription());
                    }).forEach((response) -> {
                        String var10001 = routeDefinition.getMethod();
                        response.setDescription(var10001 + " " + routeDefinition.getPath());
                    });
                    routeDefinition.metadata.put("responses", apiResponses);
                } catch (IllegalArgumentException var4) {
                    throw new RouteDefinitionException("Unable to convert " + this.responses.getClass() + " instance to " + ApiResponses.class + " instance. Details: " + var4.getMessage(), var4);
                }
            }

        }

        private void buildPredicates(RouteDefinition routeDefinition) {
            Map<String, String> predicatesMap = RouteDefinition.convertKeyValueListToMap(this.predicates);
            if (StringUtils.hasText(this.path)) {
                if (predicatesMap.containsKey("Path")) {
                    throw new RouteDefinitionException("Conflicting configuration for key: Path");
                }

                routeDefinition.predicates.add(RouteDefinition.convertToKeyValueString("Path", this.path));
            }

            if (StringUtils.hasText(this.method)) {
                if (predicatesMap.containsKey("Method")) {
                    throw new RouteDefinitionException("Conflicting configuration for key: Method");
                }

                routeDefinition.predicates.add(RouteDefinition.convertToKeyValueString("Method", this.method));
            }

        }

        private void buildFilters(RouteDefinition routeDefinition) {
            if (routeDefinition.filters.stream().noneMatch((filter) -> {
                return filter.startsWith("StripPrefix");
            })) {
                routeDefinition.filters.add(0, RouteDefinition.convertToKeyValueString("StripPrefix", "1"));
            }

            if (StringUtils.hasText(this.rateLimit)) {
                routeDefinition.filters.add(RouteDefinition.convertToKeyValueString("RateLimit", this.rateLimit));
            }

            if (this.basicAuth != null) {
                routeDefinition.filters.add(RouteDefinition.convertToKeyValueString("BasicAuth", this.basicAuth));
            }

            if (this.ssoEnabled) {
                routeDefinition.filters.add(0, "SsoLogin");
            }

            if (this.tokenRelay) {
                routeDefinition.filters.add("TokenRelay");
            }

        }
    }
}

