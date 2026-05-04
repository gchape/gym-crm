package tech.provokedynamic.gymcrm.component;

import jakarta.validation.*;

import java.io.Closeable;
import java.util.Set;
import java.util.stream.Collectors;

public enum EntityValidator implements Closeable {
    INSTANCE;

    private final Validator validator;
    private final ValidatorFactory validatorFactory;

    EntityValidator() {
        this.validatorFactory = Validation.buildDefaultValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ConstraintViolationException(message, violations);
        }
    }

    @Override
    public void close() {
        validatorFactory.close();
    }
}
