package tech.provokedynamic.gymcrm.security;

import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenBlacklist {

    private final Set<String> blacklisted = ConcurrentHashMap.newKeySet();

    public void blacklist(String token) {
        blacklisted.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklisted.contains(token);
    }
}
