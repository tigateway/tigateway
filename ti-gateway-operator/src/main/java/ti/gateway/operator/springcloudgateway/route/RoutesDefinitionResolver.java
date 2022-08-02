package ti.gateway.operator.springcloudgateway.route;

import ti.gateway.operator.springcloudgateway.apis.ObjectReferenceConverter;
import ti.gateway.operator.springcloudgateway.apis.TanzuVmwareComV1Api;
import ti.gateway.operator.springcloudgateway.gateway.SecretLister;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGateway;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayMapping;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayRouteConfig;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayRouteConfigSpec;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayRouteConfigSpecOpenapi;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayRouteConfigSpecRoutes;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayRouteConfigSpecService;
import ti.gateway.operator.springcloudgateway.util.ReferenceResolver;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.V1ObjectReference;
import io.kubernetes.client.openapi.models.V1Secret;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.AbstractMap.SimpleEntry;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.collections4.ListUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

public class RoutesDefinitionResolver {
    private static final Logger LOG = LoggerFactory.getLogger(RoutesDefinitionResolver.class);
    private final TanzuVmwareComV1Api scgApi;
    private final ApiServiceUriBuilder serviceUriBuilder;
    private final SecretLister secretLister;

    public RoutesDefinitionResolver(TanzuVmwareComV1Api scgApi, ApiServiceUriBuilder serviceUriBuilder, SecretLister secretLister) {
        this.scgApi = scgApi;
        this.serviceUriBuilder = serviceUriBuilder;
        this.secretLister = secretLister;
    }

    public RoutesDefinition getRoutes(V1SpringCloudGatewayMapping mapping, V1SpringCloudGatewayRouteConfig routeConfig) {
        V1Secret secret = this.getBasicAuthSecret(routeConfig);
        return this.getRoutes(mapping, routeConfig, secret);
    }

    public RoutesDefinition getRoutes(V1SpringCloudGatewayMapping mapping) throws UnprocessableRouteException {
        try {
            V1SpringCloudGatewayRouteConfig routeConfig = this.getRouteConfigFromMapping(mapping);
            V1Secret secret = this.getBasicAuthSecret(routeConfig);
            return this.getRoutes(mapping, routeConfig, secret);
        } catch (ApiException var4) {
            if (var4.getCode() != 404) {
                throw new UnprocessableRouteException(String.format("Failed to retrieve RouteConfig %s for Mapping %s", mapping.getSpec().getRouteConfigRef(), mapping.getMetadata().getName()), var4);
            } else {
                LOG.info("RouteConfig {} specified in mapping {}/{} doesn't exist. No action needed.", new Object[]{mapping.getSpec().getRouteConfigRef(), mapping.getMetadata().getNamespace(), mapping.getMetadata().getName()});
                return this.getRoutes(mapping, (V1SpringCloudGatewayRouteConfig)null, (V1Secret)null);
            }
        }
    }

    private RoutesDefinition getRoutes(V1SpringCloudGatewayMapping mapping, V1SpringCloudGatewayRouteConfig routeConfig, V1Secret basicAuthSecret) throws UnprocessableRouteException {
        if (routeConfig == null) {
            return RoutesDefinition.from(generateRoutePrefix(mapping), (V1SpringCloudGatewayRouteConfig)null, (V1Secret)null);
        } else {
            if (this.hasValidServiceName(routeConfig)) {
                this.updateServiceUriForEachRoute(routeConfig);
            } else {
                this.logInvalidConfiguration(routeConfig);
            }

            this.resolveSpec(routeConfig);
            return RoutesDefinition.from(generateRoutePrefix(mapping), routeConfig, basicAuthSecret);
        }
    }

    private boolean hasValidServiceName(V1SpringCloudGatewayRouteConfig routeConfig) {
        return routeConfig.getSpec() != null && routeConfig.getSpec().getService() != null && StringUtils.hasText(routeConfig.getSpec().getService().getName());
    }

    private void logInvalidConfiguration(V1SpringCloudGatewayRouteConfig routeConfig) {
        if (routeConfig.getSpec() != null && routeConfig.getSpec().getService() != null) {
            String namespace = routeConfig.getSpec().getService().getNamespace();
            boolean namespacePresent = StringUtils.hasText(namespace);
            Integer port = routeConfig.getSpec().getService().getPort();
            boolean portPresent = port != null;
            Iterator var6 = routeConfig.getSpec().getRoutes().iterator();

            while(var6.hasNext()) {
                V1SpringCloudGatewayRouteConfigSpecRoutes route = (V1SpringCloudGatewayRouteConfigSpecRoutes)var6.next();
                if (!StringUtils.hasText(route.getUri())) {
                    if (namespacePresent) {
                        LOG.warn("Invalid RouteConfig: Missing service.name, ignoring service.namespace");
                    }

                    if (portPresent) {
                        LOG.warn("Invalid RouteConfig: Missing service.name, ignoring service.port");
                    }
                }
            }
        }

    }

    private V1Secret getBasicAuthSecret(V1SpringCloudGatewayRouteConfig routeConfig) throws UnprocessableRouteException {
        V1Secret secret = null;
        if (this.hasBasicAuth(routeConfig)) {
            String namespace = routeConfig.getMetadata().getNamespace();
            String secretName = routeConfig.getSpec().getBasicAuth().getSecret();
            secret = this.secretLister.getSecret(namespace, secretName);
            if (secret == null) {
                String error = String.format("Failed to find secret '%s' in the '%s' namespace.", secretName, namespace);
                LOG.error(error);
                throw new UnprocessableRouteException(error, (Throwable)null);
            }
        }

        return secret;
    }

    private boolean hasBasicAuth(V1SpringCloudGatewayRouteConfig routeConfig) {
        return routeConfig != null && routeConfig.getSpec() != null && routeConfig.getSpec().getBasicAuth() != null && routeConfig.getSpec().getBasicAuth().getSecret() != null;
    }

    public Map<V1SpringCloudGateway, RoutesDefinition> getRoutesForGateways() throws ApiException {
        Map<V1ObjectReference, V1SpringCloudGatewayRouteConfig> scgRouteConfigsMap = this.mapRouteConfigObjectRefToRouteConfig();
        Map<V1ObjectReference, List<RoutesDefinition>> scgRefToScgRouteConfigsMap = this.mapGatewayObjectRefToRouteDefinitionList(scgRouteConfigsMap);
        Map<V1SpringCloudGateway, RoutesDefinition> routesForGatewaysMap = this.mapAllGatewaysToCorrespondingRoutesDefinition(scgRefToScgRouteConfigsMap);
        return routesForGatewaysMap;
    }

    private Map<V1SpringCloudGateway, RoutesDefinition> mapAllGatewaysToCorrespondingRoutesDefinition(Map<V1ObjectReference, List<RoutesDefinition>> scgRefToScgRouteConfigsMap) throws ApiException {
        return (Map<V1SpringCloudGateway, RoutesDefinition>)this.scgApi.listSpringCloudGatewayForAllNamespaces((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, (String)null, (String)null, (Integer)null, (Boolean)null).getItems().stream().map((scg) -> {
            RoutesDefinition routesDefinition = (RoutesDefinition)((List<RoutesDefinition>)scgRefToScgRouteConfigsMap.getOrDefault(ObjectReferenceConverter.toObjectReference(scg.getMetadata()), Collections.emptyList())).stream().reduce(new RoutesDefinition(), (routesDef1, routesDef2) -> {
                routesDef1.getRouteDefinitions().addAll(routesDef2.getRouteDefinitions());
                return routesDef1;
            });
            return new SimpleEntry<V1SpringCloudGateway, RoutesDefinition>(scg, routesDefinition);
        }).filter((scgToRoutesDefinition) -> {
            return !((RoutesDefinition)scgToRoutesDefinition.getValue()).getRouteDefinitions().isEmpty();
        }).collect(Collectors.toMap(SimpleEntry::getKey, SimpleEntry::getValue));
    }

    private Map<V1ObjectReference, List<RoutesDefinition>> mapGatewayObjectRefToRouteDefinitionList(Map<V1ObjectReference, V1SpringCloudGatewayRouteConfig> scgRouteConfigsMap) throws ApiException {
        return (Map<V1ObjectReference, List<RoutesDefinition>>)this.scgApi.listSpringCloudGatewayMappingForAllNamespaces((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, (String)null, (String)null, (Integer)null, (Boolean)null).getItems().stream().collect(Collectors.groupingBy((scgMapping) -> {
            return ObjectReferenceConverter.toObjectReference(scgMapping.getMetadata().getNamespace(), scgMapping.getSpec().getGatewayRef().getNamespace(), scgMapping.getSpec().getGatewayRef().getName());
        }, Collectors.mapping((scgMapping) -> {
            return RoutesDefinition.from(generateRoutePrefix(scgMapping), (V1SpringCloudGatewayRouteConfig)scgRouteConfigsMap.get(ObjectReferenceConverter.toObjectReference(scgMapping.getMetadata().getNamespace(), scgMapping.getSpec().getRouteConfigRef().getNamespace(), scgMapping.getSpec().getRouteConfigRef().getName())), (V1Secret)null);
        }, Collectors.filtering(Objects::nonNull, Collectors.toList()))));
    }

    private Map<V1ObjectReference, V1SpringCloudGatewayRouteConfig> mapRouteConfigObjectRefToRouteConfig() throws ApiException {
        Map<V1ObjectReference, V1SpringCloudGatewayRouteConfig> scgRouteConfigsMap = (Map<V1ObjectReference, V1SpringCloudGatewayRouteConfig>)this.scgApi.listSpringCloudGatewayRouteConfigForAllNamespaces((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, (String)null, (String)null, (Integer)null, (Boolean)null).getItems().stream().map(this::resolveSpec).collect(Collectors.toMap((routeConfig) -> {
            return ObjectReferenceConverter.toObjectReference(routeConfig.getMetadata());
        }, Function.identity()));
        return scgRouteConfigsMap;
    }

    private static String generateRoutePrefix(V1SpringCloudGatewayMapping mapping) {
        String mappingName = mapping.getMetadata().getName();
        String mappingNamespace = mapping.getMetadata().getNamespace();
        return mappingNamespace + "-" + mappingName + "-";
    }

    private V1SpringCloudGatewayRouteConfig getRouteConfigFromMapping(V1SpringCloudGatewayMapping mapping) throws ApiException {
        String routeConfigName = mapping.getSpec().getRouteConfigRef().getName();
        String routeConfigNamespace = mapping.getSpec().getRouteConfigRef().getNamespace();
        if (routeConfigNamespace == null) {
            routeConfigNamespace = mapping.getMetadata().getNamespace();
        }

        return this.scgApi.readNamespacedSpringCloudGatewayRouteConfig(routeConfigName, routeConfigNamespace, (String)null, (String)null);
    }

    private void updateServiceUriForEachRoute(V1SpringCloudGatewayRouteConfig routeConfig) throws UnprocessableRouteException {
        String serviceName = routeConfig.getSpec().getService().getName();
        String serviceNamespace = routeConfig.getSpec().getService().getNamespace();
        if (!StringUtils.hasLength(serviceNamespace)) {
            serviceNamespace = routeConfig.getMetadata().getNamespace();
        }

        String serviceUri;
        try {
            Integer port = routeConfig.getSpec().getService().getPort();
            serviceUri = this.serviceUriBuilder.build(serviceNamespace, serviceName, port);
        } catch (UriBuildingException var7) {
            throw new UnprocessableRouteException(String.format("Failed to build service uri for route config \"%s/%s\"", routeConfig.getMetadata().getNamespace(), routeConfig.getMetadata().getName()), var7);
        }

        Iterator var8 = routeConfig.getSpec().getRoutes().iterator();

        while(var8.hasNext()) {
            V1SpringCloudGatewayRouteConfigSpecRoutes r = (V1SpringCloudGatewayRouteConfigSpecRoutes)var8.next();
            if (r.getUri() != null) {
                LOG.debug("Route already has URI {} configured, skipping automatic URI generation", r.getUri());
            } else {
                LOG.debug("Configuring route with URI {}", serviceUri);
                r.setUri(serviceUri);
            }
        }

    }

    private V1SpringCloudGatewayRouteConfig resolveSpec(V1SpringCloudGatewayRouteConfig routeConfig) {
        if (routeConfig != null) {
            V1SpringCloudGatewayRouteConfigSpec spec = routeConfig.getSpec();
            if (spec != null && spec.getRoutes() != null) {
                List<V1SpringCloudGatewayRouteConfigSpecRoutes> routes = spec.getRoutes();
                if (spec.getService() != null) {
                    this.processServiceProperties(spec.getService(), routes);
                }

                if (spec.getOpenapi() != null) {
                    this.processOpenApiReferences(spec.getOpenapi(), routes);
                }
            }
        }

        return routeConfig;
    }

    private void processServiceProperties(V1SpringCloudGatewayRouteConfigSpecService service, List<V1SpringCloudGatewayRouteConfigSpecRoutes> routes) {
        List<String> servicePredicates = service.getPredicates();
        List<String> serviceFilters = service.getFilters();
        Boolean serviceSso = service.getSsoEnabled();
        Iterator var6 = routes.iterator();

        while(var6.hasNext()) {
            V1SpringCloudGatewayRouteConfigSpecRoutes r = (V1SpringCloudGatewayRouteConfigSpecRoutes)var6.next();
            List filters;
            if (servicePredicates != null) {
                filters = (List)Objects.requireNonNullElse(r.getPredicates(), Collections.emptyList());
                r.setPredicates(ListUtils.union(servicePredicates, filters));
            }

            if (serviceFilters != null) {
                filters = (List)Objects.requireNonNullElse(r.getFilters(), Collections.emptyList());
                r.setFilters(ListUtils.union(serviceFilters, filters));
            }

            if (serviceSso != null && r.getSsoEnabled() == null) {
                r.setSsoEnabled(serviceSso);
            }
        }

    }

    private void processOpenApiReferences(V1SpringCloudGatewayRouteConfigSpecOpenapi openapi, List<V1SpringCloudGatewayRouteConfigSpecRoutes> routes) {
        ReferenceResolver openapiResolver = new ReferenceResolver(openapi);
        Objects.requireNonNull(openapiResolver);
        routes.forEach(openapiResolver::resolveRoute);
    }
}

