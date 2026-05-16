package tech.provokedynamic.gymcrm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dao.UserDao;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

@Component
public class CredentialGenerator {

    private static final Random RANDOM;

    private static final int PASSWORD_LENGTH = 10;

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final Logger log = LoggerFactory.getLogger(CredentialGenerator.class);


    static {
        try {
            RANDOM = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private final UserDao userDao;

    public CredentialGenerator(
            @Qualifier("userDaoImpl") UserDao userDao
    ) {
        this.userDao = userDao;
    }

    public String generateUsername(String firstName, String lastName) {
        var base = firstName + "." + lastName;

        if (!userDao.existsByUsernameIncludingDeleted(base)) {
            log.debug("Generated username '{}'", base);
            return base;
        }

        int suffix = 1;
        while (userDao.existsByUsernameIncludingDeleted(base + suffix)) {
            suffix++;
        }

        var username = base + suffix;

        log.debug("Username '{}' taken, generated '{}'", base, username);

        return username;
    }

    public String generatePassword() {
        var sb = new StringBuilder(PASSWORD_LENGTH);

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return sb.toString();
    }
}
