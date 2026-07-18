package tech.provokedynamic.gymcrmauthorizationserver.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrmauthorizationserver.entity.AuthUser;
import tech.provokedynamic.gymcrmauthorizationserver.repository.AuthUserRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private static final String TRAINEE_DISCRIMINATOR = "trainee";

    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username '{}'", username);

        AuthUser user = authUserRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> {
                    log.warn("Authentication lookup failed: '{}' not found or inactive", username);
                    return new UsernameNotFoundException(username);
                });

        String role = TRAINEE_DISCRIMINATOR.equalsIgnoreCase(user.getUserType())
                ? "ROLE_TRAINEE"
                : "ROLE_TRAINER";

        log.debug("Loaded user '{}' with role '{}'", username, role);

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
    }
}
