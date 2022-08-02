package ti.gateway.operator.springcloudgateway.mapping;

import ti.gateway.operator.springcloudgateway.apis.TanzuVmwareComV1Api;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayMapping;
import ti.gateway.operator.springcloudgateway.models.V1SpringCloudGatewayMappingList;
import io.kubernetes.client.openapi.ApiException;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

public class MappingLister {
    private final Logger LOG = LoggerFactory.getLogger(MappingLister.class);
    private final TanzuVmwareComV1Api mappingV1Api;

    public MappingLister(TanzuVmwareComV1Api mappingV1Api) {
        this.mappingV1Api = mappingV1Api;
    }

    public Collection<V1SpringCloudGatewayMapping> listForGateway(String gatewayNamespace, String gatewayName) {
        this.LOG.info("Listing mapping for SCG {} in namespace {}", gatewayName, gatewayNamespace);

        try {
            V1SpringCloudGatewayMappingList list = this.mappingV1Api.listSpringCloudGatewayMappingForAllNamespaces((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, (String)null, (String)null, (Integer)null, (Boolean)null);
            return (Collection)list.getItems().stream().filter((item) -> {
                return this.isAssociatedToGateway(item, gatewayNamespace, gatewayName);
            }).collect(Collectors.toSet());
        } catch (ApiException var4) {
            this.LOG.error("Failed to list SpringCloudGatewayMapping objects", var4);
            throw new RuntimeException("Failed to list SpringCloudGatewayMapping objects", var4);
        }
    }

    public Collection<V1SpringCloudGatewayMapping> allMappings() {
        try {
            V1SpringCloudGatewayMappingList list = this.mappingV1Api.listSpringCloudGatewayMappingForAllNamespaces((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, (String)null, (String)null, (Integer)null, (Boolean)null);
            return new HashSet(list.getItems());
        } catch (ApiException var2) {
            if (var2.getCode() == HttpStatus.NOT_FOUND.value()) {
                return new HashSet();
            } else {
                this.LOG.error("Failed to list SpringCloudGatewayMapping objects", var2);
                throw new RuntimeException("Failed to list SpringCloudGatewayMapping objects", var2);
            }
        }
    }

    public Collection<V1SpringCloudGatewayMapping> listForRouteConfig(String routeConfigNamespace, String routeConfigName) {
        this.LOG.info("Listing mapping for SCGRC {} in namespace {}", routeConfigName, routeConfigNamespace);

        try {
            V1SpringCloudGatewayMappingList list = this.mappingV1Api.listSpringCloudGatewayMappingForAllNamespaces((Boolean)null, (String)null, (String)null, (String)null, (Integer)null, (String)null, (String)null, (String)null, (Integer)null, (Boolean)null);
            return (Collection)list.getItems().stream().filter((item) -> {
                return this.isAssociatedToRouteConfig(item, routeConfigNamespace, routeConfigName);
            }).collect(Collectors.toSet());
        } catch (ApiException var4) {
            this.LOG.error("Failed to list SpringCloudGatewayMapping objects", var4);
            throw new RuntimeException("Failed to list SpringCloudGatewayMapping objects", var4);
        }
    }

    private boolean isAssociatedToGateway(V1SpringCloudGatewayMapping mapping, String gatewayNamespace, String gatewayName) {
        if (mapping.getSpec() != null && mapping.getSpec().getGatewayRef() != null) {
            String namespace = mapping.getSpec().getGatewayRef().getNamespace() != null ? mapping.getSpec().getGatewayRef().getNamespace() : mapping.getMetadata().getNamespace();
            return gatewayNamespace.equals(namespace) && gatewayName.equals(mapping.getSpec().getGatewayRef().getName());
        } else {
            return false;
        }
    }

    private boolean isAssociatedToRouteConfig(V1SpringCloudGatewayMapping mapping, String routeConfigNamespace, String routeConfig) {
        if (mapping.getSpec() != null && mapping.getSpec().getRouteConfigRef() != null) {
            String namespace = mapping.getSpec().getRouteConfigRef().getNamespace() != null ? mapping.getSpec().getRouteConfigRef().getNamespace() : mapping.getMetadata().getNamespace();
            return routeConfigNamespace.equals(namespace) && routeConfig.equals(mapping.getSpec().getRouteConfigRef().getName());
        } else {
            return false;
        }
    }
}

