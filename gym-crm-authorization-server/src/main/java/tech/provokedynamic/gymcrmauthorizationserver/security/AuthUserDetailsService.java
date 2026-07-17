package tech.provokedynamic.gymcrmauthorizationserver.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tech.provokedynamic.gymcrmauthorizationserver.entity.AuthUser;
import tech.provokedynamic.gymcrmauthorizationserver.repository.AuthUserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthUserDetailsService implements UserDetailsService {

    private static final String TRAINEE_DISCRIMINATOR = "trainee";

    private final AuthUserRepository authUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser user = authUserRepository.findByUsernameAndActiveTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        String role = TRAINEE_DISCRIMINATOR.equalsIgnoreCase(user.getUserType())
                ? "ROLE_TRAINEE"
                : "ROLE_TRAINER";

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(role)))
                .build();
    }
}
