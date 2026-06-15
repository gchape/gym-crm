package tech.provokedynamic.gymcrm.security;

import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@NullMarked
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_SECONDS = 300;
    private final ConcurrentHashMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public void onSuccess(String username) {
        attempts.remove(username);
    }

    public void onFailure(String username) {
        attempts.compute(username, (_, current) -> {
            int count = current == null ? 1 : current.count() + 1;
            Instant blockedAt = count >= MAX_ATTEMPTS ? Instant.now() : null;
            return new LoginAttempt(count, blockedAt == null && current != null ? current.blockedAt() : Objects.requireNonNull(blockedAt));
        });
    }

    public boolean isBlocked(String username) {
        var attempt = attempts.get(username);
        if (attempt == null) return false;

        if (Instant.now().isAfter(attempt.blockedAt().plusSeconds(BLOCK_DURATION_SECONDS))) {
            attempts.remove(username);
            return false;
        }

        return true;
    }

    private record LoginAttempt(int count, Instant blockedAt) {
    }
}
