package tech.provokedynamic.gymcrm.exception;

public class AlreadyDeactivatedException extends RuntimeException {
    public AlreadyDeactivatedException(String username) {
        super("User is already inactive: " + username);
    }
}
