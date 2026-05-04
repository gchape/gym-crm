package tech.provokedynamic.gymcrm.component;

import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.entity.User;

import java.util.List;
import java.util.random.RandomGenerator;

@Component
public class CredentialGenerator {
    private static final int PASSWORD_LENGTH = 10;

    private static final RandomGenerator RANDOM = RandomGenerator.getDefault();

    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public String generateUsername(String firstName, String lastName, List<? extends User> existing) {
        var base = firstName + "." + lastName;

        long count = existing.stream()
                .filter(u -> u.getFirstName().equals(firstName) && u.getLastName().equals(lastName))
                .count();

        return count == 0 ? base : base + count;
    }

    public String generatePassword() {
        var password = new StringBuilder();

        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            password.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
        }

        return password.toString();
    }
}
