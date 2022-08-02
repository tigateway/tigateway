package ti.gateway.kubernetes.info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.autoconfigure.info.ProjectInfoProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Properties;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties({ProjectInfoProperties.class})
public class CustomBuildInfoConfiguration {
    private final ProjectInfoProperties properties;

    public CustomBuildInfoConfiguration(ProjectInfoProperties properties) {
        this.properties = properties;
    }

    @ConditionalOnResource(
            resources = {"${spring.info.build.location:classpath:META-INF/build-info.properties}"}
    )
    @ConditionalOnProperty({"com.vmware.tanzu.springcloudgateway.version"})
    @Bean
    public BuildProperties buildProperties(@Value("${com.vmware.tanzu.springcloudgateway.version}") String gatewayVersion) throws Exception {
        Properties buildProperties = this.loadFrom(this.properties.getBuild().getLocation(), "build", this.properties.getBuild().getEncoding());
        buildProperties.put("version", gatewayVersion);
        return new BuildProperties(buildProperties);
    }

    protected Properties loadFrom(Resource location, String prefix, Charset encoding) throws IOException {
        prefix = prefix.endsWith(".") ? prefix : prefix + ".";
        Properties source = this.loadSource(location, encoding);
        Properties target = new Properties();
        Iterator<String> iterator = source.stringPropertyNames().iterator();

        while(iterator.hasNext()) {
            String key = (String) iterator.next();
            if (key.startsWith(prefix)) {
                target.put(key.substring(prefix.length()), source.get(key));
            }
        }

        return target;
    }

    private Properties loadSource(Resource location, Charset encoding) throws IOException {
        return encoding != null ? PropertiesLoaderUtils.loadProperties(new EncodedResource(location, encoding)) : PropertiesLoaderUtils.loadProperties(location);
    }

}
