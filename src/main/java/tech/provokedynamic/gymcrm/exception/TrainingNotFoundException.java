package tech.provokedynamic.gymcrm.exception;

public class TrainingNotFoundException extends RuntimeException {
    public TrainingNotFoundException(Long id) {
        super("Training not found: " + id);
    }
}
