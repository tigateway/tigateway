package io.pivotal.spring.cloud.gateway.security;

import io.pivotal.spring.cloud.gateway.jwt.JwtHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class RolesExtractor {
    private static final String ATTRIBUTE_SEPARATOR = ".";
    private static final String ATTRIBUTE_SEPARATOR_REGEX = "\\.";
    private final String rolesUserAttributeName;

    public RolesExtractor(@Value("${sso.roles-attribute-name}") String rolesUserAttributeName) {
        this.rolesUserAttributeName = rolesUserAttributeName;
    }

    public Set<String> rolesFromClaim(String token) {
        return this.rolesFromClaim(JwtHelper.getClaims(token));
    }

    public Set<String> rolesFromClaim(Map<String, Object> claims) {
        if (claims == null) {
            return Collections.emptySet();
        } else if (this.rolesUserAttributeName.contains(".")) {
            String[] rolesPath = this.rolesUserAttributeName.split("\\.");
            if (rolesPath.length > 0) {
                Object claim = claims.get(rolesPath[0]);
                Iterable<String> strings = this.rolesFromMap((Map) claim, rolesPath, 1);
                return this.asSet(strings);
            } else {
                return Collections.emptySet();
            }
        } else {
            return Set.copyOf(JwtHelper.getClaimAsList(claims.get(this.rolesUserAttributeName)));
        }
    }

    private Set<String> asSet(Iterable<String> values) {
        Set<String> objects = new HashSet<>();
        Iterator iterator = values.iterator();

        while (iterator.hasNext()) {
            String value = (String) iterator.next();
            objects.add(value);
        }

        return objects;
    }

    public Set<String> rolesFromClaim(OidcUser oidcUser) {
        Object claimRoles;
        Object userInfoRoles;
        if (this.rolesUserAttributeName.contains(".")) {
            String[] rolesPath = this.rolesUserAttributeName.split("\\.");
            if (rolesPath.length > 0) {
                claimRoles = this.rolesFromMap((Map) oidcUser.getClaim(rolesPath[0]), rolesPath, 1);
                userInfoRoles = this.rolesFromMap(oidcUser.getUserInfo().getClaims(), rolesPath, 0);
            } else {
                claimRoles = userInfoRoles = Collections.emptyList();
            }
        } else {
            claimRoles = this.rolesFromClaims(oidcUser);
            userInfoRoles = this.rolesFromUserInfo(oidcUser);
        }

        return (Set) Stream.concat(StreamSupport.stream(((Iterable) claimRoles).spliterator(), false), StreamSupport.stream(((Iterable) userInfoRoles).spliterator(), false)).collect(Collectors.toSet());
    }

    private Set<String> rolesFromClaims(OidcUser oidcUser) {
        Object rolesClaim = oidcUser.getClaim(this.rolesUserAttributeName);
        if (rolesClaim instanceof String) {
            return Set.of((String) rolesClaim);
        } else {
            return !(rolesClaim instanceof Iterable) ? Collections.emptySet() : Set.copyOf((Collection) rolesClaim);
        }
    }

    private Iterable<String> rolesFromUserInfo(OidcUser oidcUser) {
        if (oidcUser.getUserInfo() == null) {
            return Collections.emptySet();
        } else {
            Object rolesAttribute = oidcUser.getUserInfo().getClaim(this.rolesUserAttributeName);
            return (Iterable) (!(rolesAttribute instanceof Iterable) ? Collections.emptySet() : (Iterable) rolesAttribute);
        }
    }

    private Iterable<String> rolesFromMap(Map<String, Object> claims, String[] rolesPath, int index) {
        if (claims == null) {
            return Collections.emptyList();
        } else {
            Object roles = null;
            if (index == rolesPath.length - 1) {
                roles = claims.get(rolesPath[index]);
            } else {
                Object value = claims.get(rolesPath[index]);
                if (value instanceof Map) {
                    roles = this.rolesFromMap((Map) value, rolesPath, index + 1);
                }
            }

            if (roles instanceof String) {
                return Set.of(JwtHelper.getClaimAsString(roles));
            } else {
                return (Iterable) (roles instanceof Collection ? (Iterable) roles : Collections.emptySet());
            }
        }
    }
}
