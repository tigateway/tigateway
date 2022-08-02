package ti.gateway.kubernetes.jwt;

import com.nimbusds.jose.Header;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JwtHelper {

    private static final Logger log = LoggerFactory.getLogger(JwtHelper.class);

    public JwtHelper() {
    }

    public static String cleanupHeaderValue(String headerValue) {
        headerValue = headerValue.replaceFirst("^(?i)bearer", "");
        headerValue = headerValue.replaceAll("\\s+", "");
        return headerValue;
    }

    private static JWT parse(String tokenString) throws ParseException {
        if (!StringUtils.hasText(tokenString)) {
            throw new ParseException("Empty token", 0);
        } else {
            return JWTParser.parse(tokenString);
        }
    }

    public static Map<String, Object> getClaims(String token) {
        try {
            return parse(token).getJWTClaimsSet().getClaims();
        } catch (ParseException var2) {
            log.error("Could not parse token", var2);
            return Collections.emptyMap();
        }
    }

    public static Object getClaimValue(String tokenString, String claim) {
        try {
            JWT token = parse(tokenString);
            Object value = token.getJWTClaimsSet().getClaim(claim);
            if (value == null) {
                Header h = token.getHeader();
                if (h instanceof JWSHeader) {
                    JWSHeader jwsHeader = (JWSHeader)token.getHeader();
                    value = jwsHeader.getKeyID();
                }
            }

            return value;
        } catch (Exception var6) {
            log.error("Could not parse token", var6);
            return null;
        }
    }

    public static String getClaimAsString(String token, String claimName) {
        Object claimValue = getClaimValue(token, claimName);
        return getClaimAsString(claimValue);
    }

    public static String getClaimAsString(Object claimValue) {
        if (claimValue instanceof String) {
            return (String)claimValue;
        } else if (claimValue instanceof List) {
            log.warn("Only using first value of claim collection, ignoring the rest");
            return (String)((List)claimValue).get(0);
        } else if (claimValue instanceof String[]) {
            log.warn("Only using first value of claim collection, ignoring the rest");
            return ((String[])claimValue)[0];
        } else {
            log.debug("Could not extract claim value: incompatible type, expected String, String[] or List<String>");
            return null;
        }
    }

    public static List<String> getClaimAsList(Object claimValue) {
        if (claimValue instanceof String) {
            return ((String)claimValue).contains(",") ? (List) Arrays.stream(((String)claimValue).split(",")).map(String::trim).collect(Collectors.toList()) : List.of((String)claimValue);
        } else if (claimValue instanceof List) {
            return (List)claimValue;
        } else if (claimValue instanceof String[]) {
            return Arrays.asList((String[])claimValue);
        } else if (!(claimValue instanceof Long) && !(claimValue instanceof Double)) {
            log.debug("Could not extract claim value: incompatible type, expected String, String[] or List<String>");
            return List.of();
        } else {
            return List.of(String.valueOf(claimValue));
        }
    }

}
