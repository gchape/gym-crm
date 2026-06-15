package tech.provokedynamic.gymcrm.security;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tech.provokedynamic.gymcrm.entity.User;

import java.util.Collection;
import java.util.List;

@NullMarked
public class GymCRMUserDetails implements UserDetails, CredentialsContainer {

    private final String username;

    private @Nullable String password;

    public GymCRMUserDetails(User user) {
        username = user.getUsername();
        password = user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    @NullUnmarked
    public @Nullable String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void eraseCredentials() {
        password = null;
    }
}
