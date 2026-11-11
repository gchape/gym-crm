package tech.provokedynamic.gymcrm.exception;

public class AlreadyActivatedException extends RuntimeException {
    public AlreadyActivatedException(String username) {
        super("User is already active: " + username);
    }
}
