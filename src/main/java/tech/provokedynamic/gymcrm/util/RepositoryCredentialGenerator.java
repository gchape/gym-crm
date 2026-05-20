package tech.provokedynamic.gymcrm.util;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.provokedynamic.gymcrm.repository.UserRepository;

import java.security.SecureRandom;
import java.util.Random;

@RequiredArgsConstructor
public class RepositoryCredentialGenerator implements CredentialGenerator {

    private static final Logger log = LoggerFactory.getLogger(RepositoryCredentialGenerator.class);

    private static final int PASSWORD_LENGTH = 10;
    private static final Random RANDOM = new SecureRandom();

    private final UserRepository userRepository;

    public String generateUsername(String firstName, String lastName) {
        String base = firstName + "." + lastName;

        if (!userRepository.existsByUsernameIncludingDeleted(base)) {
            log.debug("Generated username '{}'", base);
            return base;
        }

        int suffix = 1;
        while (userRepository.existsByUsernameIncludingDeleted(base + suffix)) {
            suffix++;
        }

        String username = base + suffix;

        log.debug("Username '{}' taken, generated '{}'", base, username);

        return username;
    }

    public String generatePassword() {
        var sb = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            if (RANDOM.nextBoolean()) {
                char letter = (char) ('a' + RANDOM.nextInt(26));

                if (RANDOM.nextBoolean()) {
                    letter = (char) (letter - 32);
                }

                sb.append(letter);
            } else {
                int digit = RANDOM.nextInt(10);

                sb.append(digit);
            }
        }

        return sb.toString();
    }
}
