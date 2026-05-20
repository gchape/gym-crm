package tech.provokedynamic.gymcrm.validation.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.Synchronized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.validation.RequestValidator;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RequestValidatorImpl implements RequestValidator {

    private static final Logger log = LoggerFactory.getLogger(RequestValidatorImpl.class);

    private final Validator validator;

    public RequestValidatorImpl(Validator validator) {
        this.validator = validator;
    }

    @Synchronized
    public <T extends Request> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            log.warn("Validation failed for {}: {}", object.getClass().getSimpleName(), message);
            throw new ConstraintViolationException(message, violations);
        }
    }
}
