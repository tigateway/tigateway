package ti.gateway.kubernetes.security;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import reactor.core.publisher.Mono;

class RolesJwtAuthenticationConverter implements Converter<Jwt, Mono<? extends AbstractAuthenticationToken>> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RolesJwtAuthenticationConverter.class);
    private final RolesExtractor rolesExtractor;
    final Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter;

    public RolesJwtAuthenticationConverter(RolesExtractor rolesExtractor, Converter<Jwt, ? extends Mono<? extends AbstractAuthenticationToken>> jwtAuthenticationConverter) {
        this.rolesExtractor = rolesExtractor;
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    public Mono<? extends AbstractAuthenticationToken> convert(Jwt source) {
        Mono<JwtAuthenticationToken> jwtAuthenticationTokenMono = this.jwtAuthenticationConverter.convert(source).map((authToken) -> {
            List<SimpleGrantedAuthority> claimAuthorities = (List<SimpleGrantedAuthority>) this.rolesExtractor.rolesFromClaim(source.getClaims()).stream().map((role) -> {
                return new SimpleGrantedAuthority("ROLE_" + role);
            }).collect(Collectors.toList());
            LOGGER.debug("Role Authorities found: {}", claimAuthorities);
            Set<GrantedAuthority> newAuthorities = new HashSet<>(authToken.getAuthorities());
            newAuthorities.addAll(claimAuthorities);
            return new JwtAuthenticationToken(source, newAuthorities);
        });
        return jwtAuthenticationTokenMono;
    }
}
