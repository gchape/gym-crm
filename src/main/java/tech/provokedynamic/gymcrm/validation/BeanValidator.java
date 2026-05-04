package tech.provokedynamic.gymcrm.validation;

import jakarta.validation.*;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public enum BeanValidator implements Closeable {
    INSTANCE;

    private static final Logger log = LoggerFactory.getLogger(BeanValidator.class);

    private final Validator validator;
    private final ValidatorFactory validatorFactory;

    BeanValidator() {
        this.validatorFactory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    public <T> void validate(T object) {
        Objects.requireNonNull(object, "Object to validate must not be null");

        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));

            log.error("Validation failed for {}: {}", object.getClass().getSimpleName(), message);
            throw new ConstraintViolationException(message, violations);
        }
    }

    @Override
    public void close() {
        validatorFactory.close();
    }
}
