package tech.provokedynamic.gymcrm.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.provokedynamic.gymcrm.dto.Request;

import java.util.Set;
import java.util.stream.Collectors;

public enum RequestValidator {
    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(RequestValidator.class);

    public <T extends Request> void validate(T object) {
        @lombok.Cleanup var validatorFactory = Validation.buildDefaultValidatorFactory();
        Set<ConstraintViolation<T>> violations = validatorFactory.getValidator().validate(object);

        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));

            log.error("Validation failed for {}: {}", object.getClass().getSimpleName(), message);
            throw new ConstraintViolationException(message, violations);
        }
    }
}
