package ti.gateway.operator.springcloudgateway.apis;

import io.kubernetes.client.extended.leaderelection.LeaderElectionConfig;
import io.kubernetes.client.extended.leaderelection.LeaderElector;
import io.kubernetes.client.extended.leaderelection.Lock;
import io.kubernetes.client.extended.leaderelection.resourcelock.LeaseLock;
import io.kubernetes.client.openapi.ApiClient;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ti.gateway.operator.springcloudgateway.gateway.OperatorProperties;

public class LeaderElection {
  private static final Logger LOG = LoggerFactory.getLogger(LeaderElection.class);

  public LeaderElection() {}

  public static LeaderElector configureLeaderElector(
      ApiClient apiClient, OperatorProperties operatorProperties, String appName) {
    String namespace = operatorProperties.getInstallNamespace();
    String lockHolderIdentityName = null;

    try {
      String var10000 = InetAddress.getLocalHost().getHostName();
      lockHolderIdentityName = var10000 + "-" + appName;
    } catch (UnknownHostException var7) {
      LOG.error("Error determining hostname for leader election identity", var7);
    }

    Lock lock = new LeaseLock(namespace, appName, lockHolderIdentityName, apiClient);
    LeaderElectionConfig leaderElectionConfig =
        new LeaderElectionConfig(
            lock, Duration.ofMillis(25000L), Duration.ofMillis(20000L), Duration.ofMillis(5000L));
    return new LeaderElector(leaderElectionConfig);
  }
}
