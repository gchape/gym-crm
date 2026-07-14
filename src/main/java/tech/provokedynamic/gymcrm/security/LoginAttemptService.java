package tech.provokedynamic.gymcrm.security;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Component
@NullMarked
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long BLOCK_DURATION_SECONDS = 10;

    private final ConcurrentHashMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public void onSuccess(String username) {
        attempts.remove(username);
    }

    public void onFailure(String username) {
        attempts.compute(username, (_, current) -> {
            if (current == null) {
                return new LoginAttempt(1, null);
            }
            int count = current.count() + 1;
            if (count < MAX_ATTEMPTS) {
                return new LoginAttempt(count, null);
            }
            if (current.blockedAt == null) {
                return new LoginAttempt(count, Instant.now());
            }
            return new LoginAttempt(count, current.blockedAt);
        });
    }

    public boolean isBlocked(String username) {
        var attempt = attempts.get(username);
        if (attempt == null || attempt.blockedAt() == null) {
            return false;
        }
        if (Instant.now().isAfter(attempt.blockedAt().plusSeconds(BLOCK_DURATION_SECONDS))) {
            attempts.remove(username);
            return false;
        }
        return true;
    }

    private record LoginAttempt(int count, @Nullable Instant blockedAt) {
    }
}
