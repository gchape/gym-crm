package tech.provokedynamic.gymcrm.service;

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

import java.time.Instant;
import java.util.List;

/** Authenticates active accounts and issues short-lived, role-bearing RSA JWTs. */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtLoginService implements LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final AuthProperties properties;

    @Override
    @Transactional(readOnly = true)
    public String login(String username, String rawPassword) {
        log.debug("Login attempt for username '{}'", username);

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Login failed for '{}': user not found or inactive", username);
                    return new AuthenticationException("Invalid credentials");
                });

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            log.warn("Login failed for '{}': password mismatch", username);
            throw new AuthenticationException("Invalid credentials");
        }

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
