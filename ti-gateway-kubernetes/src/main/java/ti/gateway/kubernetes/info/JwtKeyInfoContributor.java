package ti.gateway.kubernetes.info;

import ti.gateway.kubernetes.jwt.JwtKeyEnabled;
import ti.gateway.kubernetes.jwt.LocalFileJwtKeysLocator;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Stream;

@Component
@JwtKeyEnabled
public class JwtKeyInfoContributor implements InfoContributor {
    private final LocalFileJwtKeysLocator localFileJwtKeysLocator;

    public JwtKeyInfoContributor(LocalFileJwtKeysLocator localFileJwtKeysLocator) {
        this.localFileJwtKeysLocator = localFileJwtKeysLocator;
    }


    @Override
    public void contribute(Info.Builder builder) {
        Stream<Map<String, String>> keys = this.localFileJwtKeysLocator.getKeyIds().stream().map((id) -> {
            return Map.of("id", id, "lastRefreshTime", this.formatDate(this.localFileJwtKeysLocator.lastRefreshTime()));
        });
        builder.withDetail("jwtkeys", keys);
    }

    private String formatDate(long millis) {
        Date date = new Date(millis);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        return formatter.format(date);
    }
}
