package ti.gateway.kubernetes.ratelimit;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.util.StringUtils;

class RateLimiterProperties implements HasRouteId {
    private int limit = 0;
    private Duration duration = Duration.ofSeconds(1L);
    private String routeId;
    private String keyLocation;
    private String claim;
    private String header;
    private int xForwardedForMaxTrustedIndex = 1;
    private final List<String> ipAddresses = new ArrayList<>();
    private static final String CLAIM_KEY = "claim:";
    private static final String HEADER_KEY = "header:";
    private static final String IP_KEY = "IPs:";
    private static final String NUMBER_REGEX = "\\d+";
    private static final int DEFAULT_X_FORWARDED_FOR_MAX_TRUSTED_INDEX = 1;

    RateLimiterProperties() {
    }

    public String getClaim() {
        return this.claim;
    }

    public boolean hasClaim() {
        return StringUtils.hasText(this.claim);
    }

    public String getHeader() {
        return this.header;
    }

    public boolean hasHeader() {
        return StringUtils.hasText(this.header);
    }

    public List<String> getIPs() {
        return this.ipAddresses;
    }

    public boolean hasIPs() {
        return !this.ipAddresses.isEmpty();
    }

    public int getXForwardedForMaxTrustedIndex() {
        return this.xForwardedForMaxTrustedIndex;
    }

    public String getKeyLocation() {
        return this.keyLocation;
    }

    public void setKeyLocation(String keyLocation) {
        this.keyLocation = keyLocation;
        this.parseKeyLocation();
    }

    public int getLimit() {
        return this.limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Duration getDuration() {
        return this.duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getRouteId() {
        return this.routeId;
    }

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    private void parseKeyLocation() {
        this.claim = null;
        this.header = null;
        this.ipAddresses.clear();
        this.xForwardedForMaxTrustedIndex = 1;
        if (StringUtils.hasText(this.keyLocation) && this.keyLocation.startsWith("{") && this.keyLocation.endsWith("}")) {
            String parsedKeyLocation = this.keyLocation.substring(1, this.keyLocation.length() - 1);
            if (parsedKeyLocation.startsWith("claim:")) {
                this.parseClaim(parsedKeyLocation);
            } else if (parsedKeyLocation.startsWith("header:")) {
                this.parseHeader(parsedKeyLocation);
            } else if (parsedKeyLocation.startsWith("IPs:")) {
                this.parseIPs(parsedKeyLocation);
            }
        }

    }

    private void parseClaim(String parsedKeyLocation) {
        String parsedClaim = parsedKeyLocation.substring("claim:".length()).trim();
        if (StringUtils.hasText(parsedClaim)) {
            this.claim = parsedClaim;
        }

    }

    private void parseHeader(String parsedKeyLocation) {
        String parsedHeader = parsedKeyLocation.substring("header:".length()).trim();
        if (StringUtils.hasText(parsedHeader)) {
            this.header = parsedHeader;
        }

    }

    private void parseIPs(String parsedKeyLocation) {
        List<String> parsedIpTokens = Arrays.asList(parsedKeyLocation.substring("IPs:".length()).split(";"));
        List<String> modifiableIpList = new ArrayList<>(parsedIpTokens);
        if (modifiableIpList.size() > 0 && ((String) modifiableIpList.get(0)).trim().matches("\\d+")) {
            int parsedXForwardedForMaxTrustedIndex = Integer.parseInt(((String) modifiableIpList.get(0)).trim());
            if (parsedXForwardedForMaxTrustedIndex > 0) {
                this.xForwardedForMaxTrustedIndex = parsedXForwardedForMaxTrustedIndex;
                modifiableIpList.remove(modifiableIpList.get(0));
            }
        }

        Iterator<String> iterator = modifiableIpList.iterator();

        while (iterator.hasNext()) {
            String ip = (String) iterator.next();
            if (StringUtils.hasText(ip)) {
                this.ipAddresses.add(ip.trim());
            }
        }

    }
}
