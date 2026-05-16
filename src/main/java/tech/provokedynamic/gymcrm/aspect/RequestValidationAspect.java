package tech.provokedynamic.gymcrm.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tech.provokedynamic.gymcrm.dto.Request;
import tech.provokedynamic.gymcrm.validation.RequestValidator;

@Aspect
@Component
public class RequestValidationAspect {
    private static final Logger log = LoggerFactory.getLogger(RequestValidationAspect.class);

    private final RequestValidator validator;

    public RequestValidationAspect(RequestValidator validator) {
        this.validator = validator;
    }

    @Before("tech.provokedynamic.gymcrm.aspect.pointcuts.ServicePointcuts.validateInService()")
    public void validate(@NonNull JoinPoint joinPoint) {
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof Request request) {
                log.debug("Validating {} in {}",
                        request.getClass().getSimpleName(),
                        joinPoint.getSignature().getName());
                validator.validate(request);
            }
        }
    }
}
