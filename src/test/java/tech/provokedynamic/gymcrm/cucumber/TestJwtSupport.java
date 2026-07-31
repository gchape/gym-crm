package tech.provokedynamic.gymcrm.cucumber;

import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;

/**
 * Builds a {@link RequestPostProcessor} that injects a fake, already-decoded
 * JWT authentication into MockMvc requests, matching the roles-claim shape
 * gym-crm's own JwtConfig now issues, without needing a real JwtEncoder/
 * JwtDecoder round trip in this component test.
 */
final class TestJwtSupport {

    private TestJwtSupport() {
    }

    static RequestPostProcessor trainerToken(String... roles) {
        String role = roles.length == 0 ? "ROLE_TRAINER" : roles[0];
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .subject("test-subject")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(300))
                        .claim("roles", java.util.List.of(role)))
                .authorities(new org.springframework.security.core.authority.SimpleGrantedAuthority(role));
    }
}
