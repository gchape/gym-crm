package tech.provokedynamic.gymcrm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tech.provokedynamic.gymcrm.annotation.ValidateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.dto.request.*;
import tech.provokedynamic.gymcrm.exception.AuthenticationException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.UserRepository;

/** Verifies the current password before persisting a BCrypt-encoded replacement. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordApplicationService implements PasswordService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    @ValidateRequest
    public void updatePassword(ChangePasswordRequest request) {
        String username = request.username();

        log.debug("updatePassword called for username={}", username);

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Password update failed: user not found username={}", username);
                    return new UserDoesNotExistException(username);
                });

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            log.warn("Password update rejected for username={}: current password mismatch", username);
            throw new AuthenticationException("Invalid current password");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));

        log.info("Password updated successfully for username={}", username);
    }
}
