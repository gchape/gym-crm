package tech.provokedynamic.gymcrm.aspect;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dto.request.*;
import tech.provokedynamic.gymcrm.validation.InputValidator;

/** Enforces command validation for annotated application services, including non-HTTP callers. */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RequestValidationAspect implements Ordered {

    private final InputValidator<ValidatedRequest> validator;

    @Before("tech.provokedynamic.gymcrm.aspect.pointcuts.ServicePointcuts.validateInService()")
    public void validate(@NonNull JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof ValidatedRequest request) {
                log.trace("Validating {} in {}",
                        request.getClass().getSimpleName(),
                        joinPoint.getSignature().getName());
                validator.validate(request);
            }
        }
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
