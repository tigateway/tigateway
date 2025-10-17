package ti.gateway.kubernetes.ingress;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1IngressList;
import io.kubernetes.client.openapi.models.V1ListMeta;
import io.kubernetes.client.util.Watch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Ingress资源监听器
 * 监听Kubernetes Ingress资源的变化并触发路由刷新
 */
@Component
public class IngressWatcher {

    private static final Logger logger = LoggerFactory.getLogger(IngressWatcher.class);

    private final NetworkingV1Api networkingV1Api;
    private final IngressRouteDefinitionLocator routeDefinitionLocator;
    private final ApplicationEventPublisher eventPublisher;
    private final String namespace;
    private final ExecutorService executorService;
    private volatile boolean running = false;
    private Watch<V1Ingress> watch;

    public IngressWatcher(
            NetworkingV1Api networkingV1Api,
            IngressRouteDefinitionLocator routeDefinitionLocator,
            ApplicationEventPublisher eventPublisher,
            @Value("${spring.cloud.gateway.kubernetes.ingress.namespace:default}") String namespace) {
        this.networkingV1Api = networkingV1Api;
        this.routeDefinitionLocator = routeDefinitionLocator;
        this.eventPublisher = eventPublisher;
        this.namespace = namespace;
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "ingress-watcher");
            t.setDaemon(true);
            return t;
        });
    }

    @PostConstruct
    public void startWatching() {
        if (running) {
            return;
        }

        running = true;
        executorService.submit(this::watchIngressChanges);
        logger.info("Started watching Ingress resources in namespace: {}", namespace);
    }

    @PreDestroy
    public void stopWatching() {
        running = false;
        
        if (watch != null) {
            try {
                watch.close();
            } catch (Exception e) {
                logger.warn("Error closing Ingress watch: {}", e.getMessage());
            }
        }

        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }

        logger.info("Stopped watching Ingress resources");
    }

    /**
     * 监听Ingress资源变化
     */
    private void watchIngressChanges() {
        while (running) {
            try {
                // 获取当前资源版本
                V1IngressList ingressList = networkingV1Api.listNamespacedIngress(
                    namespace, 
                    null, // pretty
                    null, // allowWatchBookmarks
                    null, // continue
                    null, // fieldSelector
                    null, // labelSelector
                    null, // limit
                    null, // resourceVersion
                    null, // resourceVersionMatch
                    null, // timeoutSeconds
                    null, // watch
                    null  // sendInitialEvents
                );

                V1ListMeta listMeta = ingressList.getMetadata();
                String resourceVersion = listMeta != null ? listMeta.getResourceVersion() : null;

                logger.debug("Starting to watch Ingress resources from version: {}", resourceVersion);

                // 创建Watch对象
                watch = Watch.createWatch(
                    networkingV1Api.getApiClient(),
                    networkingV1Api.listNamespacedIngressCall(
                        namespace,
                        null, // pretty
                        null, // allowWatchBookmarks
                        null, // continue
                        null, // fieldSelector
                        null, // labelSelector
                        null, // limit
                        resourceVersion,
                        null, // resourceVersionMatch
                        null, // timeoutSeconds
                        1, // watch (1 for true, 0 for false)
                        null, // sendInitialEvents
                        null  // callback
                    ),
                    V1Ingress.class
                );

                // 处理Watch事件
                for (Watch.Response<V1Ingress> response : watch) {
                    if (!running) {
                        break;
                    }

                    V1Ingress ingress = response.object;
                    String eventType = response.type;
                    String ingressName = ingress.getMetadata().getName();

                    logger.debug("Received Ingress event: {} for {}", eventType, ingressName);

                    switch (eventType) {
                        case "ADDED":
                        case "MODIFIED":
                        case "DELETED":
                            handleIngressChange(eventType, ingress);
                            break;
                        default:
                            logger.debug("Ignoring unknown event type: {}", eventType);
                    }
                }

            } catch (ApiException e) {
                if (running) {
                    logger.error("Error watching Ingress resources: {}", e.getMessage(), e);
                    // 等待一段时间后重试
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            } catch (Exception e) {
                if (running) {
                    logger.error("Unexpected error in Ingress watcher: {}", e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 处理Ingress资源变化
     */
    private void handleIngressChange(String eventType, V1Ingress ingress) {
        String ingressName = ingress.getMetadata().getName();
        
        logger.info("Handling Ingress {} event for: {}", eventType, ingressName);

        // 刷新路由定义缓存
        routeDefinitionLocator.refreshRoutes();

        // 发布路由刷新事件
        eventPublisher.publishEvent(new RefreshRoutesEvent(this));

        logger.info("Routes refreshed due to Ingress {} event for: {}", eventType, ingressName);
    }

    /**
     * 手动触发路由刷新
     */
    public void refreshRoutes() {
        logger.info("Manually triggering route refresh");
        routeDefinitionLocator.refreshRoutes();
        eventPublisher.publishEvent(new RefreshRoutesEvent(this));
    }
}
