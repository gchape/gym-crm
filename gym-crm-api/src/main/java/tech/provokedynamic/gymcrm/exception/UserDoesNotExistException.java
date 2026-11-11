package tech.provokedynamic.gymcrm.exception;

public class UserDoesNotExistException extends RuntimeException {
    public UserDoesNotExistException(String username) {
        super("User does not exist: " + username);
    }
}
