package tech.provokedynamic.gymcrm.exception;

public class TrainingTypeNotFoundException extends RuntimeException {
    public TrainingTypeNotFoundException(String name) {
        super("Training type not found: " + name);
    }
}
