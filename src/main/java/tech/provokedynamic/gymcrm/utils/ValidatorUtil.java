package tech.provokedynamic.gymcrm.utils;

import jakarta.validation.*;
import org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator;

import java.io.Closeable;
import java.util.Set;

public enum ValidatorUtil implements Closeable {
    INSTANCE;

    private final Validator validator;

    private final ValidatorFactory validatorFactory;

    ValidatorUtil() {
        this.validatorFactory = Validation.byDefaultProvider()
                .configure()
                .messageInterpolator(new ParameterMessageInterpolator())
                .buildValidatorFactory();
        this.validator = validatorFactory.getValidator();
    }

    public <T> void validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .findAny()
                    .get();
            throw new ConstraintViolationException(message, violations);
        }
    }

    @Override
    public void close() {
        validatorFactory.close();
    }
}
