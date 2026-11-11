package tech.provokedynamic.gymcrm.aspect;

import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dto.request.*;
import tech.provokedynamic.gymcrm.validation.InputValidator;

/** Enforces command validation for annotated application services, including non-HTTP callers. */
@Aspect
@Component
@RequiredArgsConstructor
public class RequestValidationAspect implements Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestValidationAspect.class);

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
