package io.pivotal.spring.cloud.gateway.session;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.hazelcast.config.Config;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.KubernetesConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.kubernetes.KubernetesProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.oauth2.client.jackson2.OAuth2ClientJackson2Module;
import org.springframework.security.web.jackson2.WebJackson2Module;
import org.springframework.security.web.server.jackson2.WebServerJackson2Module;
import org.springframework.session.MapSession;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

@EnableSpringWebSession
@Configuration(
        proxyBeanMethods = false
)
public class SessionConfiguration {
    private static final String SCG_SESSION_COOKIE_NAME = "SCG-SESSION";
    private static final Logger logger = LoggerFactory.getLogger(SessionConfiguration.class);

    public SessionConfiguration() {
    }

    @Bean
    WebSessionIdResolver cookieSessionIdResolver() {
        CookieWebSessionIdResolver cookieWebSessionIdResolver = new CookieWebSessionIdResolver();
        cookieWebSessionIdResolver.setCookieName("SCG-SESSION");
        return cookieWebSessionIdResolver;
    }

    @Configuration(
            proxyBeanMethods = false
    )
    static class HazelcastReactiveSessionConfiguration {
        HazelcastReactiveSessionConfiguration() {
        }

        @Bean
        @ConditionalOnProperty({"hazelcast.network.join.kubernetes.service-name"})
        Config hazelcastConfig(@Value("${hazelcast.network.join.kubernetes.service-name}") String hazelcastServiceName, @Value("${hazelcast.network.join.kubernetes.service-port}") Integer hazelcastServicePort, @Value("${hazelcast.network.join.kubernetes.namespace}") String hazelcastNamespace) {
            SessionConfiguration.logger.info("hazelcastServiceName = {}", hazelcastServiceName);
            Config config = new Config();
            config.setClassLoader(MapSession.class.getClassLoader());
            JoinConfig networkJoin = config.getNetworkConfig().getJoin();
            networkJoin.getMulticastConfig().setEnabled(false);

            ((KubernetesConfig) ((KubernetesConfig) ((KubernetesConfig) ((KubernetesConfig) networkJoin.getKubernetesConfig().setEnabled(true))
                    .setProperty(KubernetesProperties.SERVICE_NAME.key(), hazelcastServiceName))
                    .setProperty(KubernetesProperties.SERVICE_PORT.key(), Integer.toString(hazelcastServicePort)))
                    .setProperty(KubernetesProperties.NAMESPACE.key(), hazelcastNamespace))
                    .setProperty(KubernetesProperties.USE_NODE_NAME_AS_EXTERNAL_ADDRESS.key(), "false");

            config.getPartitionGroupConfig().setEnabled(false);
            config.setProperty("hazelcast.wait.seconds.before.join", "1");
            config.setProperty("hazelcast.discovery.public.ip.enabled", "false");
            return config;
        }

        @Bean
        @ConditionalOnMissingBean({Config.class})
        Config hazelcastFallbackConfig() {
            SessionConfiguration.logger.info("Using fallback multicast Hazelcast config");
            Config config = new Config();
            JoinConfig networkJoin = config.getNetworkConfig().getJoin();
            networkJoin.getMulticastConfig().setEnabled(true);
            return config;
        }

        @Bean
        HazelcastReactiveSessionRepository hazelcastReactiveSessionRepository(HazelcastInstance hazelcastInstance, SessionProperties sessionProperties) {
            HazelcastReactiveSessionRepository hazelcastReactiveSessionRepository = new HazelcastReactiveSessionRepository(hazelcastInstance, getSessionObjectMapper());
            long defaultMaxInactiveInterval = sessionProperties.getTimeout().getSeconds();
            hazelcastReactiveSessionRepository.setDefaultMaxInactiveInterval((int) defaultMaxInactiveInterval);
            return hazelcastReactiveSessionRepository;
        }

        public static ObjectMapper getSessionObjectMapper() {
            return (new ObjectMapper()).registerModule(new JavaTimeModule()).registerModule(new CoreJackson2Module()).registerModule(new WebJackson2Module()).registerModule(new WebServerJackson2Module()).registerModule(new OAuth2ClientJackson2Module()).addMixIn(MapSession.class, SessionConfiguration.HazelcastReactiveSessionConfiguration.MapSessionMixIn.class).setVisibility(PropertyAccessor.FIELD, Visibility.ANY).activateDefaultTyping(BasicPolymorphicTypeValidator.builder().allowIfBaseType(Object.class).build());
        }

        @JsonIgnoreProperties({"attributeNames", "expired"})
        private abstract static class MapSessionMixIn {
            private MapSessionMixIn() {
            }
        }
    }
}
