package tech.provokedynamic.gymcrm.exception;

/**
 * Thrown when an authenticated user attempts to act on a resource
 * (another user's profile, password, active-status, etc.) that isn't
 * their own. Distinct from {@link AuthenticationException}, which covers
 * "who are you" failures — this covers "you are who you say, but you may
 * not do this."
 */
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
