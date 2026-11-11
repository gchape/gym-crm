package tech.provokedynamic.gymcrm.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dto.request.*;

import java.util.stream.Collectors;

/** Adapts Jakarta Bean Validation to the application command validation contract. */
@Slf4j
@Component
@RequiredArgsConstructor
public class BeanValidationRequestValidator implements InputValidator<ValidatedRequest> {

    private final jakarta.validation.Validator validator;

    @Override
    public void validate(ValidatedRequest request) {
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
