package tech.provokedynamic.gymcrm.validation;

import jakarta.validation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.provokedynamic.gymcrm.dto.Request;

import java.util.Set;
import java.util.stream.Collectors;

public enum RequestValidator {
    INSTANCE();

    private static final Logger log = LoggerFactory.getLogger(RequestValidator.class);

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    private final Validator validator = factory.getValidator();

    public <T extends Request> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));

            log.error("Validation failed for {}: {}", object.getClass().getSimpleName(), message);
            throw new ConstraintViolationException(message, violations);
        }
    }
}
