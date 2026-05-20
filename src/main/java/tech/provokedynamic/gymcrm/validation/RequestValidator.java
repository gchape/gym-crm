package tech.provokedynamic.gymcrm.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.provokedynamic.gymcrm.dto.Request;

import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RequestValidator implements Validator<Request> {

    private final static Logger log = LoggerFactory.getLogger(RequestValidator.class);

    private final jakarta.validation.Validator validator;

    @Override
    public void validate(Request request) {
        var violations = validator.validate(request);

        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));

            log.warn("Validation failed for {}: {}", request.getClass().getSimpleName(), message);
            throw new ConstraintViolationException(message, violations);
        }
    }
}
