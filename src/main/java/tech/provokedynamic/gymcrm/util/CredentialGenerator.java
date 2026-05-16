package tech.provokedynamic.gymcrm.util;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dao.UserDao;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component
public class CredentialGenerator {
    private static final Random RANDOM;

    private static final int PASSWORD_LENGTH = 10;

    private static final Charset DEFAULT_CHARSET = UTF_8;

    static {
        try {
            RANDOM = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private final UserDao userDao;

    public CredentialGenerator(UserDao userDao) {
        this.userDao = userDao;
    }

    public String generateUsername(String firstName, String lastName) {
        var base = firstName + "." + lastName;

        if (!userDao.existsByUsernameIncludingDeleted(base)) {
            return base;
        }

        int suffix = 1;
        while (userDao.existsByUsernameIncludingDeleted(base + suffix)) {
            suffix++;
        }

        return base + suffix;
    }

    public String generatePassword() {
        float maxBytesPerChar = DEFAULT_CHARSET.newEncoder().maxBytesPerChar();

        byte[] bytes = new byte[(int) (PASSWORD_LENGTH * maxBytesPerChar)];

        RANDOM.nextBytes(bytes);

        return new String(bytes, DEFAULT_CHARSET);
    }
}
