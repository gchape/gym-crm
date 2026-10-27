package tech.provokedynamic.gymcrm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.config.AuthProperties;
import tech.provokedynamic.gymcrm.entity.Trainee;
import tech.provokedynamic.gymcrm.exception.AuthenticationException;
import tech.provokedynamic.gymcrm.repository.UserRepository;
import tech.provokedynamic.gymcrm.service.LoginService;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final AuthProperties properties;

    @Override
    @Transactional(readOnly = true)
    public String login(String username, String rawPassword) {
        log.debug("Login attempt for username '{}'", username);

        // findByUsername already excludes soft-deleted/deactivated users
        // (see User's @SoftDelete(strategy = ACTIVE)), so a deactivated
        // trainee/trainer naturally falls through to the same "invalid
        // credentials" response as an unknown username — no separate
        // active-flag check needed here.
        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Login failed for '{}': user not found or inactive", username);
                    return new AuthenticationException("Invalid credentials");
                });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Login failed for '{}': password mismatch", username);
            throw new AuthenticationException("Invalid credentials");
        }

        // JOINED inheritance means Hibernate already materialized the
        // concrete subtype, so this is a cheap in-memory check.
        String role = user instanceof Trainee ? "ROLE_TRAINEE" : "ROLE_TRAINER";
        var now = Instant.now();

        var claims = JwtClaimsSet.builder()
                .issuer(properties.issuer())
                .subject(user.getUsername())
                .issuedAt(now)
                .expiresAt(now.plus(properties.accessTokenTtl()))
                .claim("roles", List.of(role))
                .build();

        log.info("Login succeeded for '{}' with role '{}'", username, role);

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
