package ti.gateway.operator.springcloudgateway.routeconfig;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import ti.gateway.operator.springcloudgateway.apis.EventRecorder;
import ti.gateway.operator.springcloudgateway.apis.ObjectReferenceConverter;
import ti.gateway.operator.springcloudgateway.route.RouteDefinition;
import ti.gateway.operator.springcloudgateway.route.RoutesDefinition;
import io.kubernetes.client.extended.controller.reconciler.Request;
import io.kubernetes.client.extended.event.EventType;
import io.kubernetes.client.openapi.models.V1ObjectReference;
import io.kubernetes.client.openapi.models.V1Pod;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.HttpUrl.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActuatorRoutesUpdater {
    private static final Logger LOG = LoggerFactory.getLogger(ActuatorRoutesUpdater.class);
    private static final String HOST_HEADER_NAME = "Host";
    private static final String HOST_URL_TEMPLATE = "%s-headless.%s.svc.cluster.local";
    private final int gatewayApplicationPort;
    private final EventRecorder eventRecorder;
    private final ActuatorRoutesUpdater.GatewayActuatorRoutesClient routesClient;

    public ActuatorRoutesUpdater(int gatewayApplicationPort, EventRecorder eventRecorder) {
        this.gatewayApplicationPort = gatewayApplicationPort;
        this.eventRecorder = eventRecorder;
        this.routesClient = new ActuatorRoutesUpdater.GatewayActuatorRoutesClient();
    }

    public void deleteMapping(V1Pod gatewayPod, RoutesDefinition routesDefinition, String hostHeader) throws PodUpdateException {
        String baseUrl = this.buildGatewayActuatorBaseUrl(gatewayPod);
        String routePrefix = routesDefinition.getRoutePrefix();
        boolean isChanged = this.deleteGatewayRoutes(baseUrl, routePrefix, hostHeader);
        this.refreshGatewayRoutesIfChanged(baseUrl, isChanged, hostHeader);
        LOG.info("Routes with prefix {} deleted from pod {}", routePrefix, gatewayPod.getMetadata().getName());
    }

    public void addMapping(V1Pod gatewayPod, RoutesDefinition routesDefinition, String hostHeader) throws PodUpdateException {
        String baseUrl = this.buildGatewayActuatorBaseUrl(gatewayPod);
        boolean isChanged = this.addGatewayRoutes(baseUrl, routesDefinition, hostHeader, gatewayPod);
        this.refreshGatewayRoutesIfChanged(baseUrl, isChanged, hostHeader);
        LOG.info("Routes with prefix {} added to pod {}", routesDefinition.getRoutePrefix(), gatewayPod.getMetadata().getName());
    }

    public void updateMapping(V1Pod gatewayPod, RoutesDefinition routesDefinition, String hostHeader) throws PodUpdateException {
        String baseUrl = this.buildGatewayActuatorBaseUrl(gatewayPod);
        boolean hasDeleted = this.deleteGatewayRoutes(baseUrl, routesDefinition.getRoutePrefix(), hostHeader);
        boolean hasAdded = this.addGatewayRoutes(baseUrl, routesDefinition, hostHeader, gatewayPod);
        this.refreshGatewayRoutesIfChanged(baseUrl, hasDeleted || hasAdded, hostHeader);
        LOG.info("Routes with prefix {} updated to pod {}", routesDefinition.getRoutePrefix(), gatewayPod.getMetadata().getName());
    }

    public static String buildHostHeader(Request request, V1Pod pod) {
        if (pod.getMetadata() != null && pod.getMetadata().getLabels() != null) {
            String gatewayName = (String)pod.getMetadata().getLabels().get("gateway.name");
            return String.format("%s-headless.%s.svc.cluster.local", gatewayName, request.getNamespace());
        } else {
            return "";
        }
    }

    private boolean addGatewayRoutes(String baseUrl, RoutesDefinition routesDefinition, String hostHeader, V1Pod gatewayPod) {
        List<RouteDefinition> routes = routesDefinition.getRouteDefinitions();
        if (routes != null && !routes.isEmpty()) {
            Iterator var6 = routes.iterator();

            while(var6.hasNext()) {
                RouteDefinition scgRoute = (RouteDefinition)var6.next();

                try {
                    this.routesClient.addGatewayRoute(baseUrl, scgRoute, hostHeader);
                } catch (PodUpdateException var10) {
                    String error = String.format("Failed to update route with title '%s' and uri '%s' due to: '%s'", scgRoute.getTitle(), scgRoute.getUri(), var10.getMessage());
                    LOG.debug(error);
                    this.eventRecorder.logEvent(ObjectReferenceConverter.toObjectReference(gatewayPod), (V1ObjectReference)null, "RouteUpdateException", error, EventType.Warning);
                }
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean deleteGatewayRoutes(String baseUrl, String routePrefix, String hostHeader) throws PodUpdateException {
        ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes routes = this.routesClient.getGatewayRoutesBy(baseUrl, routePrefix, hostHeader);
        Iterator var5 = routes.iterator();

        while(var5.hasNext()) {
            ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes.Route route = (ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes.Route)var5.next();
            this.routesClient.deleteGatewayRoute(baseUrl, route, hostHeader);
        }

        return !routes.isEmpty();
    }

    private void refreshGatewayRoutesIfChanged(String baseUrl, boolean isChanged, String hostHeader) throws PodUpdateException {
        if (isChanged) {
            this.routesClient.refreshGatewayRoutes(baseUrl, hostHeader);
        }

    }

    private String buildGatewayActuatorBaseUrl(V1Pod gatewayPod) {
        return (new Builder()).scheme("http").host(gatewayPod.getStatus().getPodIP()).port(this.gatewayApplicationPort).addPathSegments("actuator/gateway").build().toString();
    }

    private static class GatewayActuatorRoutesClient {
        private static final Gson JSON = new Gson();
        private static final MediaType JSON_TYPE = MediaType.get("application/json");
        private final OkHttpClient httpClient = new OkHttpClient();

        private GatewayActuatorRoutesClient() {
        }

        void addGatewayRoute(String baseUrl, RouteDefinition route, String hostHeader) throws PodUpdateException {
            HttpUrl url = this.buildActuatorRouteUrl(baseUrl, route.getId());
            okhttp3.Request request = (new okhttp3.Request.Builder()).url(url).addHeader("Host", hostHeader).post(RequestBody.create(JSON_TYPE, JSON.toJson(route))).build();
            this.call(request);
            ActuatorRoutesUpdater.LOG.debug("Route {} is added to {}", route.getId(), url);
        }

        void deleteGatewayRoute(String baseUrl, ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes.Route route, String hostHeader) throws PodUpdateException {
            HttpUrl url = this.buildActuatorRouteUrl(baseUrl, route.id);
            okhttp3.Request request = (new okhttp3.Request.Builder()).url(url).addHeader("Host", hostHeader).delete().build();
            this.call(request);
            ActuatorRoutesUpdater.LOG.debug("Route {} is deleted from {}", route.id, url);
        }

        void refreshGatewayRoutes(String baseUrl, String hostHeader) throws PodUpdateException {
            HttpUrl refreshUrl = HttpUrl.get(baseUrl).newBuilder().addPathSegment("refresh").build();
            okhttp3.Request refreshRequest = (new okhttp3.Request.Builder()).url(refreshUrl).addHeader("Host", hostHeader).post(RequestBody.create(JSON_TYPE, "")).build();
            this.call(refreshRequest);
            ActuatorRoutesUpdater.LOG.debug("Routes are refreshed with {}", refreshUrl);
        }

        private ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes getGatewayRoutes(String baseUrl, String hostHeader) throws PodUpdateException {
            HttpUrl getUrl = HttpUrl.get(baseUrl).newBuilder().addPathSegment("routes").build();
            okhttp3.Request request = (new okhttp3.Request.Builder()).url(getUrl).addHeader("Host", hostHeader).get().build();
            Response response = this.call(request);
            ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes gatewayRoutes = ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes.deserialize(response.body().charStream());
            ActuatorRoutesUpdater.LOG.debug("Routes are get from {}", getUrl);
            return gatewayRoutes;
        }

        private ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes getGatewayRoutesBy(String baseUrl, String routePrefix, String hostHeader) throws PodUpdateException {
            ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes gatewayRoutes = this.getGatewayRoutes(baseUrl, hostHeader);
            gatewayRoutes.removeIf((route) -> {
                return !route.id.startsWith(routePrefix);
            });
            return gatewayRoutes;
        }

        private HttpUrl buildActuatorRouteUrl(String baseUrl, String routeId) {
            return HttpUrl.get(baseUrl).newBuilder().addPathSegment("routes").addPathSegment(routeId).build();
        }

        private Response call(okhttp3.Request request) throws PodUpdateException {
            try {
                Response response = this.httpClient.newCall(request).execute();
                if (!response.isSuccessful()) {
                    throw new PodUpdateException(request.url().toString(), response.code(), response.message());
                } else {
                    return response;
                }
            } catch (IOException var3) {
                throw new PodUpdateException(String.format("Request to %s failed", request.url()), var3);
            }
        }

        private static class GatewayRoutes extends ArrayList<ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes.Route> {
            private GatewayRoutes() {
            }

            static ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes deserialize(Reader json) {
                return (ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes)ActuatorRoutesUpdater.GatewayActuatorRoutesClient.JSON.fromJson(json, ActuatorRoutesUpdater.GatewayActuatorRoutesClient.GatewayRoutes.class);
            }

            static class Route {
                @SerializedName("route_id")
                public String id;

                Route() {
                }

                public String toString() {
                    return "GatewayRoute{id='" + this.id + "'}";
                }
            }
        }
    }
}

