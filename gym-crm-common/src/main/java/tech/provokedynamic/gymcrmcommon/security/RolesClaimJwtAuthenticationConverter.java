package tech.provokedynamic.gymcrmcommon.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Both gym-crm and gym-crm-workload issue/validate JWTs carrying a "roles"
 * claim (e.g. ["ROLE_TRAINER"]) instead of relying solely on OAuth2 scopes.
 * This converter merges both so either claim shape is honored.
 */
public final class RolesClaimJwtAuthenticationConverter {

    private static final String ROLES_CLAIM = "roles";

    private RolesClaimJwtAuthenticationConverter() {
    }

    public static JwtAuthenticationConverter create() {
        var scopeConverter = new JwtGrantedAuthoritiesConverter();

        var converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> scopeAuthorities = scopeConverter.convert(jwt);

            List<String> roles = jwt.getClaimAsStringList(ROLES_CLAIM);
            Stream<GrantedAuthority> roleAuthorities = roles == null
                    ? Stream.empty()
                    : roles.stream().map(SimpleGrantedAuthority::new);

            return Stream.concat(scopeAuthorities.stream(), roleAuthorities)
                    .collect(Collectors.toSet());
        });
        return converter;
    }
}
