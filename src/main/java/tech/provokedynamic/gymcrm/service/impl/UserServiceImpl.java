package tech.provokedynamic.gymcrm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.exception.AuthenticationException;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.UserRepository;
import tech.provokedynamic.gymcrm.service.UserService;
import tech.provokedynamic.gymcrm.util.SecurityUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void updatePassword(Request.ChangePassword request) {
        String username = request.username();

        log.debug("updatePassword called for username={}", username);

        SecurityUtils.requireSelf(username);

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
