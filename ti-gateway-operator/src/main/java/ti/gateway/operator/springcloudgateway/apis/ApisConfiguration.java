package ti.gateway.operator.springcloudgateway.apis;

import io.kubernetes.client.extended.controller.Controller;
import io.kubernetes.client.extended.controller.ControllerManager;
import io.kubernetes.client.informer.SharedInformerFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.apis.CustomObjectsApi;
import io.kubernetes.client.openapi.apis.RbacAuthorizationV1Api;
import io.kubernetes.client.util.ClientBuilder;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import okhttp3.Protocol;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
class ApisConfiguration {
    ApisConfiguration() {
    }

    @Bean
    @Profile({"!test"})
    public ApiClient apiClient() throws IOException {
        ApiClient apiClient = ClientBuilder.standard(false).build();
        apiClient.setHttpClient(apiClient.getHttpClient().newBuilder().protocols(Arrays.asList(Protocol.HTTP_2, Protocol.HTTP_1_1)).readTimeout(Duration.ZERO).pingInterval(1L, TimeUnit.MINUTES).build());
        io.kubernetes.client.openapi.Configuration.setDefaultApiClient(apiClient);
        return apiClient;
    }

    @Bean
    AppsV1Api appsV1Api(ApiClient client) {
        return new AppsV1Api(client);
    }

    @Bean
    CoreV1Api coreV1Api(ApiClient client) {
        return new CoreV1Api(client);
    }

    @Bean
    RbacAuthorizationV1Api rbacAuthorizationApi(ApiClient client) {
        return new RbacAuthorizationV1Api(client);
    }

    @Bean
    CustomObjectsApi customObjectsApi(ApiClient apiClient) {
        return new CustomObjectsApi(apiClient);
    }

    @Bean
    TanzuVmwareComV1Api TanzuVmwareComV1Api(ApiClient client) {
        return new TanzuVmwareComV1Api(client);
    }

    @Bean
    EventRecorder eventClient(CoreV1Api coreV1Api) {
        return new EventRecorder(coreV1Api);
    }

    @Bean(
            destroyMethod = "shutdown"
    )
    ControllerManager controllerManager(SharedInformerFactory sharedInformerFactory, Controller[] controllers) {
        return new ControllerManager(sharedInformerFactory, controllers);
    }

    @Bean
    CommandLineRunner commandLineRunner(ControllerManager controllerManager) {
        return (args) -> {
            (new Thread(controllerManager, "controller manager")).start();
        };
    }
}
