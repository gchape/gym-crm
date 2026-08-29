package tech.provokedynamic.gymcrmworkload.testsupport;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;

/**
 * Shared trainer-role JWT builder for gym-crm-workload's MockMvc test suites
 * (component cucumber, cross-service integration cucumber, and any plain
 *
 * @SpringBootTest / slice tests). Lives outside both glue packages since
 * Cucumber requires glue classes to stay in their own package tree.
 */
public final class TrainerJwt {

    private TrainerJwt() {
    }

    public static RequestPostProcessor trainerToken() {
        return SecurityMockMvcRequestPostProcessors.jwt()
                .jwt(jwt -> jwt
                        .subject("trainer-test-subject")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(300))
                        .claim("roles", List.of("ROLE_TRAINER")))
                .authorities(new SimpleGrantedAuthority("ROLE_TRAINER"));
    }
}
