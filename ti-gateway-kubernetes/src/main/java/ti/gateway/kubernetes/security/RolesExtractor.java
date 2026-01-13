package ti.gateway.kubernetes.security;

import ti.gateway.kubernetes.jwt.JwtHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class RolesExtractor {
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
                @SuppressWarnings("unchecked")
                Map<String, Object> claimMap = (Map<String, Object>) claim;
                Iterable<String> strings = this.rolesFromMap(claimMap, rolesPath, 1);
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
        Iterator<String> iterator = values.iterator();

        while (iterator.hasNext()) {
            String value = iterator.next();
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
                @SuppressWarnings("unchecked")
                Map<String, Object> claimMap = (Map<String, Object>) oidcUser.getClaim(rolesPath[0]);
                claimRoles = this.rolesFromMap(claimMap, rolesPath, 1);
                userInfoRoles = this.rolesFromMap(oidcUser.getUserInfo().getClaims(), rolesPath, 0);
            } else {
                claimRoles = userInfoRoles = Collections.emptyList();
            }
        } else {
            claimRoles = this.rolesFromClaims(oidcUser);
            userInfoRoles = this.rolesFromUserInfo(oidcUser);
        }

        @SuppressWarnings("unchecked")
        Iterable<String> claimRolesIterable = (Iterable<String>) claimRoles;
        @SuppressWarnings("unchecked")
        Iterable<String> userInfoRolesIterable = (Iterable<String>) userInfoRoles;
        return Stream.concat(
                StreamSupport.stream(claimRolesIterable.spliterator(), false),
                StreamSupport.stream(userInfoRolesIterable.spliterator(), false)
        ).collect(Collectors.toSet());
    }

    private Set<String> rolesFromClaims(OidcUser oidcUser) {
        Object rolesClaim = oidcUser.getClaim(this.rolesUserAttributeName);
        if (rolesClaim instanceof String) {
            return Set.of((String) rolesClaim);
        } else if (rolesClaim instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<String> rolesCollection = (Collection<String>) rolesClaim;
            return Set.copyOf(rolesCollection);
        } else {
            return Collections.emptySet();
        }
    }

    private Iterable<String> rolesFromUserInfo(OidcUser oidcUser) {
        if (oidcUser.getUserInfo() == null) {
            return Collections.emptySet();
        } else {
            Object rolesAttribute = oidcUser.getUserInfo().getClaim(this.rolesUserAttributeName);
            if (rolesAttribute instanceof Iterable) {
                @SuppressWarnings("unchecked")
                Iterable<String> rolesIterable = (Iterable<String>) rolesAttribute;
                return rolesIterable;
            } else {
                return Collections.emptySet();
            }
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
                    @SuppressWarnings("unchecked")
                    Map<String, Object> valueMap = (Map<String, Object>) value;
                    roles = this.rolesFromMap(valueMap, rolesPath, index + 1);
                }
            }

            if (roles instanceof String) {
                return Set.of(JwtHelper.getClaimAsString(roles));
            } else if (roles instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<String> rolesCollection = (Collection<String>) roles;
                return rolesCollection;
            } else {
                return Collections.emptySet();
            }
        }
    }
}
