package tech.provokedynamic.gymcrm.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.exception.UserDoesNotExistException;
import tech.provokedynamic.gymcrm.repository.UserRepository;
import tech.provokedynamic.gymcrm.service.UserService;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public void updatePassword(Request.ChangePassword request) {
        String username = request.username();

        log.debug("updatePassword called for username={}", username);

        var user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Password update failed: user not found username={}", username);
                    return new UserDoesNotExistException(username);
                });

        String oldPassword = user.getPassword();
        String newPassword = request.newPassword();

        if (oldPassword.equals(newPassword)) {
            log.warn("Password update ignored: new password is same as old username={}", username);
            return;
        }

        user.setPassword(newPassword);

        log.info("Password updated successfully for username={}", username);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkCredentials(String username, String password) {
        log.debug("checkCredentials called for username={}", username);

        boolean valid = userRepository.existsByUsernameAndPassword(username, password);

        log.info("Credentials check completed username={} result={}", username, valid);

        return valid;
    }
}
