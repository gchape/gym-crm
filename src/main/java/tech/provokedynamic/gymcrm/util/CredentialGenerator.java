package tech.provokedynamic.gymcrm.util;

import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.entity.User;

import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.List;
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

    public String generateUsername(String firstName, String lastName, @NonNull List<? extends User> existing) {
        var base = firstName + "." + lastName;

        long count = existing.stream()
                .filter(u -> u.getUsername().startsWith(base))
                .count();

        return count == 0 ? base : base + count;
    }

    public String generatePassword() {
        float maxBytesPerChar = DEFAULT_CHARSET.newEncoder().maxBytesPerChar();

        byte[] bytes = new byte[(int) (PASSWORD_LENGTH * maxBytesPerChar)];

        RANDOM.nextBytes(bytes);

        return new String(bytes, DEFAULT_CHARSET);
    }
}
