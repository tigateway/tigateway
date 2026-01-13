package ti.gateway.kubernetes.ingress;

import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.NetworkingV1Api;
import io.kubernetes.client.openapi.models.V1Ingress;
import io.kubernetes.client.openapi.models.V1IngressList;
import io.kubernetes.client.openapi.models.V1ListMeta;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.util.Watch;
import com.google.gson.reflect.TypeToken;
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

                // 创建Watch对象 - 使用 TypeToken 指定泛型类型
                // 方法签名: listNamespacedIngressCall(String, String, Boolean, String, String, String, Integer, String, String, Boolean, Integer, Boolean, ApiCallback)
                watch = Watch.createWatch(
                    networkingV1Api.getApiClient(),
                    networkingV1Api.listNamespacedIngressCall(
                        namespace,              // String namespace
                        null,                  // String pretty
                        Boolean.TRUE,          // Boolean allowWatchBookmarks
                        null,                  // String continue
                        null,                  // String fieldSelector
                        null,                  // String labelSelector
                        null,                  // Integer limit
                        resourceVersion,       // String resourceVersion
                        null,                  // String resourceVersionMatch
                        null,                  // Boolean timeoutSeconds
                        1,                     // Integer watch (1 for true)
                        null,                  // Boolean sendInitialEvents
                        null                   // ApiCallback callback
                    ),
                    new TypeToken<Watch.Response<V1Ingress>>() {}.getType()
                );

                // 处理Watch事件 - 使用 next() 方法
                while (running) {
                    try {
                        Watch.Response<V1Ingress> response = watch.next();
                        if (response == null) {
                            // Watch 已关闭或结束，重新创建
                            logger.debug("Watch returned null, will recreate");
                            break;
                        }

                        String eventType = response.type;
                        V1Ingress ingress = response.object;
                        
                        // 处理 BOOKMARK 事件（用于更新 resourceVersion，object 可能为 null）
                        if ("BOOKMARK".equals(eventType)) {
                            if (ingress != null) {
                                V1ObjectMeta bookmarkMetadata = ingress.getMetadata();
                                if (bookmarkMetadata != null) {
                                    String bookmarkResourceVersion = bookmarkMetadata.getResourceVersion();
                                    if (bookmarkResourceVersion != null) {
                                        logger.debug("Received BOOKMARK event with resourceVersion: {}", bookmarkResourceVersion);
                                        // 可以在这里更新 resourceVersion 用于下次 Watch
                                    }
                                }
                            }
                            continue; // BOOKMARK 事件不需要处理，继续等待下一个事件
                        }
                        
                        // 对于其他事件类型，object 不应该为 null
                        // 但如果 eventType 为空或未知，且 object 为 null，可能是连接保活信号
                        if (ingress == null) {
                            if (eventType == null || eventType.isEmpty()) {
                                logger.debug("Received event with null type and object (connection keep-alive), continuing");
                                continue;
                            }
                            logger.debug("Received {} event with null ingress object, skipping", eventType);
                            continue;
                        }
                        
                        V1ObjectMeta metadata = ingress.getMetadata();
                        if (metadata == null) {
                            logger.warn("Received {} event with null metadata, skipping", eventType);
                            continue;
                        }
                        
                        String ingressName = metadata.getName();
                        if (ingressName == null) {
                            logger.warn("Received {} event with null ingress name, skipping", eventType);
                            continue;
                        }

                        logger.debug("Received Ingress event: {} for {}", eventType, ingressName);

                        switch (eventType) {
                            case "ADDED":
                            case "MODIFIED":
                            case "DELETED":
                                handleIngressChange(eventType, ingress);
                                break;
                            case "ERROR":
                                logger.warn("Received ERROR event from Watch, will recreate");
                                break; // 退出内层循环，重新创建 Watch
                            default:
                                logger.debug("Ignoring unknown event type: {}", eventType);
                        }
                    } catch (RuntimeException e) {
                        // Watch 可能因为各种原因失败（连接断开、解析错误等）
                        if (running) {
                            String errorMsg = e.getMessage();
                            String errorClass = e.getClass().getSimpleName();
                            
                            // 如果错误信息是 "Null response from the server"，这可能是正常的连接保持
                            // 或者是服务器发送的空响应（用于保持连接活跃）
                            if (errorMsg != null && (errorMsg.contains("Null response") || 
                                errorMsg.contains("null response") ||
                                errorMsg.contains("Response is null"))) {
                                logger.debug("Watch received null response (connection keep-alive), continuing: {}", errorMsg);
                                // 继续等待下一个事件，不退出循环
                                try {
                                    Thread.sleep(500); // 短暂等待后继续
                                } catch (InterruptedException ie) {
                                    Thread.currentThread().interrupt();
                                    break;
                                }
                                continue;
                            } 
                            // 检查是否是 ClassCastException（可能是类型转换问题，需要重新创建 Watch）
                            else if (e instanceof ClassCastException) {
                                logger.warn("Watch ClassCastException: {}, will recreate Watch", errorMsg);
                                break; // 重新创建 Watch
                            }
                            // 其他运行时异常
                            else {
                                logger.warn("Watch error [{}]: {}, will retry", errorClass, errorMsg);
                                break; // 重新创建 Watch
                            }
                        } else {
                            break;
                        }
                    } catch (Exception e) {
                        // 捕获其他所有异常
                        if (running) {
                            String errorMsg = e.getMessage();
                            logger.warn("Unexpected Watch exception: {}, will retry", errorMsg != null ? errorMsg : e.getClass().getSimpleName());
                        }
                        break; // 重新创建 Watch
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
        if (ingress == null || ingress.getMetadata() == null) {
            logger.warn("Cannot handle Ingress change: ingress or metadata is null");
            return;
        }
        
        V1ObjectMeta metadata = ingress.getMetadata();
        if (metadata == null) {
            logger.warn("Cannot handle Ingress change: metadata is null");
            return;
        }
        
        String ingressName = metadata.getName();
        if (ingressName == null) {
            logger.warn("Cannot handle Ingress change: ingress name is null");
            return;
        }
        
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
